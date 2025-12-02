package com.daisobook.shop.booksearch.BooksSearch.dto.request;

public record CategoryReqDTO(
        String categoryName,
        int deep,
        String preCategoryName) {
}
