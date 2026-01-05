package com.daisobook.shop.booksearch.books_search.dto.response;

public record LikeRespDTO(
        long likeId,
        long bookId,
        long userId,
        String title,
        String imageUrl
) { }
