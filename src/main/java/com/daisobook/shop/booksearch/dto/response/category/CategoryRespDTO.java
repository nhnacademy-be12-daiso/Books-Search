package com.daisobook.shop.booksearch.dto.response.category;

public record CategoryRespDTO(
        long categoryId,
        String categoryName,
        int deep,
        Long preCategoryId,
        String preCategoryName) {
}
