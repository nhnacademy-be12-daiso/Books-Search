package com.daisobook.shop.booksearch.BooksSearch.dto.response;

public record TagRespDTO(
        long bookId,
        long tagId,
        String tagName
) { }
