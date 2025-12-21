package com.daisobook.shop.booksearch.BooksSearch.search.message;

import java.util.List;

/**
 * AI 분석 요청 메시지 rabbitMQ를 통해 워커 서버로 전송
 * @param requestId
 * @param isbns
 * @param timestamp
 */
public record AiAnalysisRequestMessage(
        String requestId,
        List<String> isbns,  // 단건에서 리스트로 변경
        long timestamp
) {}