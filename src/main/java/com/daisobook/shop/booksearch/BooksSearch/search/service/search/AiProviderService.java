package com.daisobook.shop.booksearch.BooksSearch.search.service.search;

import com.daisobook.shop.booksearch.BooksSearch.search.config.AiClient;
import com.daisobook.shop.booksearch.BooksSearch.search.config.SearchUtils;
import com.daisobook.shop.booksearch.BooksSearch.search.domain.Book;
import com.daisobook.shop.booksearch.BooksSearch.search.dto.AiResultDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiProviderService {

    private final AiClient aiClient;
    private final ObjectMapper objectMapper;

    /**
     * 임베딩 생성 (Float 변환 포함)
     */
    public List<Float> generateEmbedding(String text) {
        List<Double> embedding = aiClient.generateEmbedding(text);
        return embedding.stream().map(Double::floatValue).toList();
    }

    /**
     * Rerank API 호출
     * - 최적화: 리랭커 속도 향상을 위해 텍스트 길이를 80자로 제한
     */
    public List<Map<String, Object>> rerank(String query, List<Book> candidates) {
        List<String> docTexts = candidates.stream()
                .map(b -> {
                    // HTML 태그 제거 및 80자 제한 (학교 서버 부하 감소용)
                    String desc = SearchUtils.stripHtml(b.getDescription());
                    if (desc.length() > 80) desc = desc.substring(0, 80);

                    // 제목 + 카테고리 + 짧은 설명 조합
                    return b.getTitle() + " " + b.getCategories() + " : " + desc;
                })
                .toList();
        return aiClient.rerank(query, docTexts);
    }

    /**
     * Gemini 평가 및 JSON 파싱
     * - 최적화: 프롬프트 입력량 줄임
     * - 퀄리티: 장점/주의/요약 3단 구성 적용
     */
    public Map<String, AiResultDto> evaluateBooks(String userQuery, List<Book> books) {
        String prompt = createEvaluationPrompt(userQuery, books);
        try {
            String rawResponse = aiClient.generateAnswer(prompt);

            // 응답이 없거나, 에러 상황에서 "{}"를 보냈다면 종료
            if (rawResponse == null || rawResponse.isBlank() || rawResponse.equals("{}")) {
                return Collections.emptyMap();
            }

            // 마크다운 코드 블럭 제거 (```json ... ```)
            String jsonResponse = rawResponse
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            return objectMapper.readValue(jsonResponse, new TypeReference<>() {});

        } catch (Exception e) {
            log.warn("AI 평가 파싱 실패 (기본 결과 반환): {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    // 프롬프트 생성 로직
    private String createEvaluationPrompt(String userQuery, List<Book> books) {
        StringBuilder bookInfo = new StringBuilder();
        for (Book book : books) {
            String desc = SearchUtils.stripHtml(book.getDescription());

            // AI가 읽을 분량도 최적화 (120자 제한)
            // 리랭커보단 조금 더 길게 줘서 정확한 판단 유도
            if (desc.length() > 120) desc = desc.substring(0, 120);

            bookInfo.append(String.format("| ISBN: %s | 제목: %s | 설명: %s... |\n",
                    book.getIsbn(), book.getTitle(), desc));
        }

        // 🔥 3줄 요약 프롬프트 (장점/주의/요약)
        return String.format("""
                질문: "%s"
                위 도서들 중 질문에 가장 적합한 **상위 3권**을 선정해.
                
                [규칙]
                1. **matchRate**: 질문과의 연관성을 70~99점 사이로 객관적으로 평가.
                2. **reason**: 반드시 **아래 3가지 항목을 포함하여 3줄**로 작성해. (이모지 활용)
                   - 👍 **장점**: 질문과 관련하여 이 책이 가진 가장 큰 강점.
                   - ⚠️ **주의**: 이 책이 부족하거나, 맞지 않을 수 있는 독자층 (예: 입문자에겐 어려움).
                   - 💡 **요약**: 그래서 추천하는지, 누구에게 딱인지 한 줄 결론.
                3. 응답은 JSON 형식만 반환해.
                
                [도서 목록]
                %s
                
                [JSON 예시]
                { 
                  "ISBN값": {
                    "reason": "👍 장점: 질문하신 스프링 배치 설정이 가장 상세합니다.\\n⚠️ 주의: 예제 코드가 구버전일 수 있습니다.\\n💡 요약: 실무 설정을 깊게 파고들고 싶다면 필독서!", 
                    "matchRate": 95
                  } 
                }
                """, userQuery, bookInfo.toString());
    }
}