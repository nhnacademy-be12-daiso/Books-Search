package com.daisobook.shop.booksearch.BooksSearch.dto.request;

public record UpdateCategoryReqDTO(
        String categoryName,
        int deep) {
}
