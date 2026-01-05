package com.daisobook.shop.booksearch.dto.request.category;

public record CategoryRegisterReqDTO(
        long categoryId,
        String name,
        int deep,
        Long preCategoryId
) {
}