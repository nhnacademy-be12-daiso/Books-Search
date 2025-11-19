package com.daisobook.shop.booksearch.BooksSearch.dto.response;

public record CategoryRespDTO(
        long categoryId,
        String categoryName,
        int deep,
        String preCategoryName) { }
