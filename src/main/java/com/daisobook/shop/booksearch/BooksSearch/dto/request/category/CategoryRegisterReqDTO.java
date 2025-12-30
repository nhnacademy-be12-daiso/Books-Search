package com.daisobook.shop.booksearch.BooksSearch.dto.request.category;

public record CategoryRegisterReqDTO(
        long categoryId,
        String name,
        int deep,
        Long preCategoryId
) {
}