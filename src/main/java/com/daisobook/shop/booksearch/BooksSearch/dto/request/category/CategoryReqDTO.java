package com.daisobook.shop.booksearch.BooksSearch.dto.request.category;

public record CategoryReqDTO(
        String categoryName,
        int deep,
        String preCategoryName) {
}
