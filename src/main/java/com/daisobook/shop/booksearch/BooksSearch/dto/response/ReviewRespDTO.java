package com.daisobook.shop.booksearch.BooksSearch.dto.response;

import java.time.ZonedDateTime;

public record ReviewRespDTO(
        long id,
        long bookId,
        long userId,
        String content,
        int rating,
        ZonedDateTime createdAt,
        ZonedDateTime modifiedAt
) { }