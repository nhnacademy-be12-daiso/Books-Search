package com.daisobook.shop.booksearch.books_search.search.message;

public record BookDeleteMessage(
        String requestId,
        String isbn,
        long ts,
        String reason
) {}
