package com.daisobook.shop.booksearch.BooksSearch.search.service.component.ai;

import com.daisobook.shop.booksearch.BooksSearch.search.config.AiClient;
import com.daisobook.shop.booksearch.BooksSearch.search.config.SearchUtils;
import com.daisobook.shop.booksearch.BooksSearch.search.domain.Book;
import com.daisobook.shop.booksearch.BooksSearch.search.dto.AiResultDto;
import com.daisobook.shop.booksearch.BooksSearch.search.exception.LlmAnalysisException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class LlmAnalysisClient {

    private final AiClient aiClient;
    private final ObjectMapper objectMapper;

    public Map<String, AiResultDto> analyzeBooks(String userQuery, List<Book> books) {
        try {
            String prompt = createEvaluationPrompt(userQuery, books);
            String rawResponse = aiClient.generateAnswer(prompt);

            if (rawResponse == null || rawResponse.isBlank() || rawResponse.equals("{}")) {
                // 빈 응답이 오면 로그를 남겨서 알 수 있게 함
                log.warn("⚠️ Gemini가 분석 결과로 빈 JSON({})을 반환했습니다. (모든 책이 기준 미달로 판단됨)");
                return Collections.emptyMap();
            }

            String jsonResponse = rawResponse
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            return objectMapper.readValue(jsonResponse, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("[LlmAnalysisClient] 도서 분석 실패. Query: {}", userQuery, e);
            throw new LlmAnalysisException("Gemini 분석 및 파싱 오류", e);
        }
    }

    private String createEvaluationPrompt(String userQuery, List<Book> books) {
        StringBuilder bookInfo = new StringBuilder();

        for (Book book : books) {
            String desc = SearchUtils.stripHtml(book.getDescription());
            if (desc.length() > 150) desc = desc.substring(0, 150);

            bookInfo.append(String.format("| ISBN: %s | 제목: %s | 설명: %s... |\n",
                    book.getIsbn(), book.getTitle(), desc));
        }

        return String.format("""
                질문: "%s"
                위 목록(총 %d권)을 분석해.
                
                [규칙]
                1. **matchRate**: 질문 관련성(0~99점). 관련 없으면 10점이라도 줄 것.
                2. **reason**: 아래 3가지 항목을 **줄바꿈(\\n)으로 구분된 '하나의 문자열'**로 작성.
                   - **명사형(~함)으로 짧게 끊을 것 (20자 이내).**
                   
                   - 👍 장점: (강점)
                   - ⚠️ 주의: (단점)
                   - 🎯 추천: **질문('%s')과의 연결고리.** (없으면 '관련성 낮음'이라고 명시)
                
                3. 🔥 **중요: 점수가 낮아도 절대 제외하지 말고, 목록에 있는 모든 책을 포함할 것.**
                4. 결과는 **JSON** 포맷만 반환.
                
                [도서 목록]
                %s
                
                [JSON 반환 예시]
                { 
                  "9791162244222": { 
                    "reason": "👍 장점: 풍부한 예제\\n⚠️ 주의: 구형 모델 위주\\n🎯 추천: 질문과 가장 적합함", 
                    "matchRate": 95 
                  }
                }
                """, userQuery, books.size(), userQuery, bookInfo.toString());
    }
}