package com.daisobook.shop.booksearch.dto.response.order;

public record BookReviewResponse(
        BookResponse book,
        Long orderDetailId,
        Long reviewId
) {
}