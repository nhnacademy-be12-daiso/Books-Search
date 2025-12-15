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

    public List<Float> generateEmbedding(String text) {
        List<Double> embedding = aiClient.generateEmbedding(text);
        return embedding.stream().map(Double::floatValue).toList();
    }

    public List<Map<String, Object>> rerank(String query, List<Book> candidates) {
        List<String> docTexts = candidates.stream()
                .map(b -> {
                    // [최적화 1] 리랭킹용 텍스트를 60자로 더 줄임 (속도 최우선)
                    String desc = SearchUtils.stripHtml(b.getDescription());
                    if (desc.length() > 50) desc = desc.substring(0, 50);
                    return b.getTitle() + " " + b.getCategories() + " : " + desc;
                })
                .toList();
        return aiClient.rerank(query, docTexts);
    }

    public Map<String, AiResultDto> evaluateBooks(String userQuery, List<Book> books) {
        String prompt = createEvaluationPrompt(userQuery, books);
        try {
            String rawResponse = aiClient.generateAnswer(prompt);

            if (rawResponse == null || rawResponse.isBlank() || rawResponse.equals("{}")) {
                return Collections.emptyMap();
            }

            String jsonResponse = rawResponse
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            return objectMapper.readValue(jsonResponse, new TypeReference<>() {});

        } catch (Exception e) {
            log.warn("AI 평가 파싱 실패: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private String createEvaluationPrompt(String userQuery, List<Book> books) {
        StringBuilder bookInfo = new StringBuilder();
        for (Book book : books) {
            String desc = SearchUtils.stripHtml(book.getDescription());

            // [최적화 2] AI에게 보내는 설명도 100자 -> 80자로 축소
            if (desc.length() > 80) desc = desc.substring(0, 80);

            bookInfo.append(String.format("| ISBN: %s | 제목: %s | 설명: %s... |\n",
                    book.getIsbn(), book.getTitle(), desc));
        }

        // [최적화 3] 프롬프트 다이어트 (핵심 문구형 요약)
        return String.format("""
                질문: "%s"
                위 목록에서 질문에 가장 적합한 **상위 3권**을 선정해.
                
                [규칙]
                1. **matchRate**: 50~99점 사이 평가.
                2. **reason**: **이모지 포함하여 최대한 짧게(단답형)** 작성. 긴 문장 금지.
                   - 👍 장점: (20자 이내 핵심만)
                   - ⚠️ 주의: (타겟 독자 등 짧게)
                   - 💡 요약: (강력 추천 이유)
                3. JSON만 반환.
                
                [도서 목록]
                %s
                
                [JSON 예시]
                { 
                  "ISBN": {
                    "reason": "👍 장점: 실무 보안 설정 완벽 가이드\\n⚠️ 주의: 초보자에겐 용어가 어려움\\n💡 요약: 3년차 이상 개발자 필독서", 
                    "matchRate": 95
                  } 
                }
                """, userQuery, bookInfo.toString());
    }
}