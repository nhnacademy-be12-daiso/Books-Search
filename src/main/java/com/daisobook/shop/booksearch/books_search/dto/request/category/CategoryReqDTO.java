package com.daisobook.shop.booksearch.books_search.dto.request.category;

public record CategoryReqDTO(
        String categoryName,
        int deep,
        String preCategoryName) {
}
