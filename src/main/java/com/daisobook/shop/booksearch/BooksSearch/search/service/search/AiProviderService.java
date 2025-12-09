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

    // 임베딩 (Float 변환 포함)
    public List<Float> generateEmbedding(String text) {
        List<Double> embedding = aiClient.generateEmbedding(text);
        return embedding.stream().map(Double::floatValue).toList();
    }

    // Rerank API 호출
    public List<Map<String, Object>> rerank(String query, List<Book> candidates) {
        List<String> docTexts = candidates.stream()
                .map(b -> b.getTitle() + " " + b.getCategories() + " : " + SearchUtils.stripHtml(b.getDescription()))
                .toList();
        return aiClient.rerank(query, docTexts);
    }

    // Gemini 평가 및 JSON 파싱 (안전한 버전으로 수정됨)
    public Map<String, AiResultDto> evaluateBooks(String userQuery, List<Book> books) {
        String prompt = createEvaluationPrompt(userQuery, books);
        try {
            String rawResponse = aiClient.generateAnswer(prompt);

            // 응답이 없거나, AiClient가 에러 상황에서 "{}"를 보냈다면 파싱하지 않고 종료
            if (rawResponse == null || rawResponse.isBlank() || rawResponse.equals("{}")) {
                return Collections.emptyMap();
            }

            // 마크다운 코드 블럭 제거
            String jsonResponse = rawResponse
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            return objectMapper.readValue(jsonResponse, new TypeReference<>() {});

        } catch (Exception e) {
            // 검색 기능 전체가 죽지 않도록 안전하게 처리
            log.warn("AI 평가 결과 파싱 실패 (기본 검색 결과만 반환됩니다): {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    // 프롬프트 생성 (핵심 프롬프트 로직 유지)
    private String createEvaluationPrompt(String userQuery, List<Book> books) {
        StringBuilder bookInfo = new StringBuilder();
        for (Book book : books) {
            String desc = SearchUtils.stripHtml(book.getDescription());
            if (desc.length() > 150) desc = desc.substring(0, 150);
            bookInfo.append(String.format("| ISBN: %s | 제목: %s | 카테고리: %s | 설명: %s... |\n",
                    book.getIsbn(), book.getTitle(), book.getCategories(), desc));
        }

        return String.format("""
                당신은 **통찰력 있는 도서 큐레이터**입니다. 
                사용자 질문: "%s"
                
                아래 도서 목록을 보고, 질문과의 **적합성(matchRate)**과 **추천 사유(reason)**를 JSON으로 반환하세요.
                
                [평가 가이드라인 - 매우 중요]
                1. **질문 유형 먼저 판단**:
                   - **기술/지식 질문** (예: "자바", "역사", "과학"): **엄격하게 평가.** 주제가 다르면(예: 기술 질문에 소설) 30점 이하 부여.
                   - **감성/추상 질문** (예: "가볍게 읽을 책", "힐링", "여행"): **유연하게 평가.** 에세이, 소설, 만화라도 분위기가 맞으면 85~99점 부여.
                
                2. **matchRate (점수)**:
                   - 질문 의도에 부합하면 **85~99점** (사용자 만족 유도).
                   - 조금 관련은 있으나 아쉬우면 **60~80점**.
                   - 전혀 엉뚱한 책이면 **40점 미만**.
                
                3. **reason (설명)**:
                   - 단순 요약 금지. **"사용자의 질문에 왜 이 책이 맞는지"** 연결해서 설명.
                   - (예: "여가 시간에 읽기 편한 문체와 따뜻한 내용이라 추천합니다.")
                
                [도서 목록]
                %s
                
                [응답 형식 (JSON Only)]
                {
                  "ISBN1": {"reason": "...", "matchRate": 95},
                  "ISBN2": {"reason": "...", "matchRate": 20}
                }
                """, userQuery, bookInfo.toString());
    }
}