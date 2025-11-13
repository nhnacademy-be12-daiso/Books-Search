package com.daisobook.shop.booksearch.BooksSearch.dto.request;

import java.time.ZonedDateTime;

public record ReviewReqDTO(
        long bookId,
        long userId,
        String content,
        int rating,
        ZonedDateTime createdAt,
        ZonedDateTime modifiedAt
) { }
