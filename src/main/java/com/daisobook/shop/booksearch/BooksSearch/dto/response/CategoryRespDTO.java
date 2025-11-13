package com.daisobook.shop.booksearch.BooksSearch.dto.response;

public record CategoryRespDTO(
        long bookId,
        long categoryId,
        String CategoryName
) { }
