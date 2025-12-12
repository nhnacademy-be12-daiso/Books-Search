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
        if (embedding.isEmpty()) {
            log.warn("[AiProvider] 임베딩 결과가 비어있습니다: '{}'", text);
        }
        return embedding.stream().map(Double::floatValue).toList();
    }

    public List<Map<String, Object>> rerank(String query, List<Book> candidates) {
        if (candidates.isEmpty()) return Collections.emptyList();

        List<String> docTexts = candidates.stream()
                .map(b -> b.getTitle() + " " + b.getCategories() + " : " + SearchUtils.stripHtml(b.getDescription()))
                .toList();
        return aiClient.rerank(query, docTexts);
    }

    public Map<String, AiResultDto> evaluateBooks(String userQuery, List<Book> books) {
        String prompt = createEvaluationPrompt(userQuery, books);
        String rawResponse = ""; // 에러 로그용 변수

        try {
            rawResponse = aiClient.generateAnswer(prompt);

            if (rawResponse == null || rawResponse.isBlank() || rawResponse.equals("{}")) {
                log.warn("[AiProvider] LLM 응답이 비어있거나 유효하지 않음.");
                return Collections.emptyMap();
            }

            // 마크다운 코드 블럭 제거
            String jsonResponse = rawResponse
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            return objectMapper.readValue(jsonResponse, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("[AiProvider] JSON 파싱 실패! LLM이 반환한 값:\n[{}]\n에러 메시지: {}", rawResponse, e.getMessage());
            // 여기서 LLM 응답이 이상했는지 코드 문제인지 바로 알 수 있음
            return Collections.emptyMap();
        }
    }

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
                
                [평가 가이드라인]
                1. **기술/지식 질문**: 관련 없으면 30점 이하.
                2. **감성 질문**: 분위기 맞으면 85점 이상.
                3. **JSON 형식 준수**: 반드시 아래 형식을 지킬 것.
                
                [도서 목록]
                %s
                
                [응답 형식 (JSON Only)]
                {
                  "ISBN값": {"reason": "추천 이유...", "matchRate": 95}
                }
                """, userQuery, bookInfo.toString());
    }
}