package com.daisobook.shop.booksearch.books_search.dto.response;

public record TotalDataRespDTO (
        Long totalCount,
        Long soldOutCount,
        Long newReviewCount,
        Long categoryCount
) {
}