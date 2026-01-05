package com.daisobook.shop.booksearch.dto.response;

public record LikeRespDTO(
        long likeId,
        long bookId,
        long userId,
        String title,
        String imageUrl
) { }
