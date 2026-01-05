package com.daisobook.shop.booksearch.books_search.dto.response.order;

public record OrderBookSummeryDTO (
        Long bookId,
        String title,
        Long price
) {
}
