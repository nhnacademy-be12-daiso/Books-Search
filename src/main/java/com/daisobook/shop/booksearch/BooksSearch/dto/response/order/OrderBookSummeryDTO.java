package com.daisobook.shop.booksearch.BooksSearch.dto.response.order;

public record OrderBookSummeryDTO (
        Long bookId,
        String title,
        Long price
) {
}
