package com.daisobook.shop.booksearch.BooksSearch.search.message;

public record BookDeleteMessage(
        String requestId,
        String isbn,
        long ts,
        String reason
) {}
