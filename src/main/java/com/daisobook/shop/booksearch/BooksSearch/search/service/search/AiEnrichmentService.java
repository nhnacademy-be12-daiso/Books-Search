package com.daisobook.shop.booksearch.BooksSearch.search.service.search;

import com.daisobook.shop.booksearch.BooksSearch.search.config.AiClient;
import com.daisobook.shop.booksearch.BooksSearch.search.config.SearchUtils;
import com.daisobook.shop.booksearch.BooksSearch.search.dto.AiResultDto;
import com.daisobook.shop.booksearch.BooksSearch.search.dto.BookResponseDto;
import com.daisobook.shop.booksearch.BooksSearch.search.dto.SearchResponseDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiEnrichmentService {

    private final AiClient aiClient;
    private final ObjectMapper objectMapper;

    // ★ 수정 1: AI에게 보여주는 후보는 5개로 늘림 (선택지를 주기 위해)
    // 하지만 출력은 3개만 하라고 시킬 예정 (속도 방어)
    private static final int AI_CANDIDATE_COUNT = 5;
    private static final int MIN_GUARANTEED_COUNT = 3; // 무조건 보장해야 할 개수

    public SearchResponseDto enrichWithAiAnalysis(String userQuery, SearchResponseDto basicResult) {
        List<BookResponseDto> books = basicResult.getBookList();
        if (books.isEmpty()) return basicResult;

        long start = System.currentTimeMillis();

        // 1. 평가 후보군 추출 (상위 5개)
        List<BookResponseDto> targets = books.stream().limit(AI_CANDIDATE_COUNT).toList();

        // 2. 프롬프트 생성
        String prompt = createPrompt(userQuery, targets);

        Map<String, AiResultDto> aiResults = Collections.emptyMap();
        try {
            // 3. Gemini 호출
            String jsonResponse = aiClient.generateAnswer(prompt);

            // 4. 파싱
            aiResults = parseAiResponse(jsonResponse);

        } catch (Exception e) {
            log.warn("[AiEnrichment] AI 호출/파싱 실패 (기본 멘트로 대체합니다): {}", e.getMessage());
        }

        // 5. ★ 핵심: 결과 매핑 및 '빈구멍 채우기' (Fallback Logic)
        int filledCount = 0;

        for (BookResponseDto dto : books) {
            // A. AI 결과가 있는 경우
            if (aiResults.containsKey(dto.getIsbn())) {
                AiResultDto eval = aiResults.get(dto.getIsbn());
                dto.setMatchRate(eval.matchRate());
                dto.setAiAnswer(eval.reason());
                filledCount++;
            }
            // B. AI 결과가 없지만, 3개를 채우기 위해 강제로 넣어야 하는 경우
            else if (filledCount < MIN_GUARANTEED_COUNT) {
                // 상위권 도서인데 AI가 답을 안 줬다면 강제 주입
                dto.setMatchRate(80); // 기본 점수 부여
                dto.setAiAnswer("검색어와의 연관성이 높아 추천하는 도서입니다."); // 기본 멘트
                filledCount++;
            }
        }

        // 6. 재정렬 (AI 점수 받은 애들이 위로 오도록)
        books.sort(Comparator.comparingInt(BookResponseDto::getMatchRate).reversed());
        log.info("[AiEnrichment] 평가 완료: {}ms (AI응답: {}건, 최종보장: {}건)",
                System.currentTimeMillis() - start, aiResults.size(), filledCount);

        return basicResult;
    }

    private Map<String, AiResultDto> parseAiResponse(String raw) {
        try {
            if (raw == null || raw.equals("{}") || raw.isBlank()) return Collections.emptyMap();
            String cleaned = raw.replaceAll("```json", "").replaceAll("```", "").trim();
            return objectMapper.readValue(cleaned, new TypeReference<>() {});
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private String createPrompt(String query, List<BookResponseDto> books) {
        StringBuilder sb = new StringBuilder();
        for (BookResponseDto b : books) {
            String desc = SearchUtils.stripHtml(b.getDescription());
            if (desc.length() > 80) desc = desc.substring(0, 80);
            sb.append(String.format("| ISBN: %s | 제목: %s | 설명: %s... |\n", b.getIsbn(), b.getTitle(), desc));
        }

        return String.format("""
                질문: "%s"
                위 도서 목록 중 질문에 가장 적합한 **상위 3권**을 선정해.
                
                [필수 규칙]
                1. **무조건 3권**에 대한 결과를 JSON으로 반환할 것. (절대 생략 금지)
                2. 설명(reason)은 한국어로 **한 문장으로 짧고 매력 있게** 작성해.
                3. 적합도(matchRate)는 아래 기준에 따라 **객관적으로 평가하되, 추천을 위해 최소 70점 이상**으로 부여해.
                   - **90~99점**: 질문의 키워드/의도와 정확히 일치 (강력 추천)
                   - **80~89점**: 주제는 맞으나 약간 넓거나 지엽적인 경우 (추천)
                   - **70~79점**: 직접적인 관련은 적으나 흥미로운 연관성이 있는 경우
                
                [도서 목록]
                %s
                
                [JSON 형식]
                { "ISBN값": {"reason": "짧은 추천 이유", "matchRate": 85} }
                """, query, sb.toString());
    }
}