package com.daisobook.shop.booksearch.dto.response;

public record TotalDataRespDTO (
        Long totalCount,
        Long soldOutCount,
        Long newReviewCount,
        Long categoryCount
) {
}