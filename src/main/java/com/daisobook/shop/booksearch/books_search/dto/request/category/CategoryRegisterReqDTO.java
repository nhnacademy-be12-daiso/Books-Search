package com.daisobook.shop.booksearch.books_search.dto.request.category;

public record CategoryRegisterReqDTO(
        long categoryId,
        String name,
        int deep,
        Long preCategoryId
) {
}