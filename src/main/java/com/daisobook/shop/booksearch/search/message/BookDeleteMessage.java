package com.daisobook.shop.booksearch.search.message;

public record BookDeleteMessage(
        String requestId,
        String isbn,
        long ts,
        String reason
) {}
