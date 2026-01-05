package com.daisobook.shop.booksearch.dto.response.order;

public record OrderBookSummeryDTO (
        Long bookId,
        String title,
        Long price
) {
}
