package com.daisobook.shop.booksearch.BooksSearch.search.dto;

import java.util.List;

/**
 * ES에 저장될 AI 분석 결과.
 * - Worker가 Gemini 호출 후 ES에 aiResult로 저장하고,
 * - BookSearch는 검색 결과에서 그대로 내려줍니다.
 */
public record AiAnalysisDto(
        List<String> pros,
        List<String> cons,
        List<String> recommendedFor
) {}
