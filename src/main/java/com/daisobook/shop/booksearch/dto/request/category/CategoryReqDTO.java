package com.daisobook.shop.booksearch.dto.request.category;

public record CategoryReqDTO(
        String categoryName,
        int deep,
        String preCategoryName) {
}
