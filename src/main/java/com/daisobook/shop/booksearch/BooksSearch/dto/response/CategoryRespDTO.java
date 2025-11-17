package com.daisobook.shop.booksearch.BooksSearch.dto.response;

public record CategoryRespDTO(
        long categoryId,
        String CategoryName,
        int deep) { }
