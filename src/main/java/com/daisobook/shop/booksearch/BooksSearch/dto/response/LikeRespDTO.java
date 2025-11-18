package com.daisobook.shop.booksearch.BooksSearch.dto.response;

public record LikeRespDTO(
        long bookId,
        long userId,
        String title,
        String imageUrl
) { }
