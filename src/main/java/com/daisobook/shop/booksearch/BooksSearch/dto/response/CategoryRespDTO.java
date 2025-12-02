package com.daisobook.shop.booksearch.BooksSearch.dto.response;

public record CategoryRespDTO(
        long categoryId,
        String categoryName,
        int deep,
        Long preCategoryId,
        String preCategoryName) {
}
