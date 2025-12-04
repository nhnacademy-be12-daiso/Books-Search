package com.daisobook.shop.booksearch.BooksSearch.dto.response.order;

public record BookReviewResponse(
        BookResponse book,
        long orderDetailId,
        Long reviewId
) {
}