package com.daisobook.shop.booksearch.books_search.dto.response.order;

public record BookReviewResponse(
        BookResponse book,
        Long orderDetailId,
        Long reviewId
) {
}