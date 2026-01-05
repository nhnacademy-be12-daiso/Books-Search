package com.daisobook.shop.booksearch.books_search.dto.response.category;

public record CategoryRespDTO(
        long categoryId,
        String categoryName,
        int deep,
        Long preCategoryId,
        String preCategoryName) {
}
