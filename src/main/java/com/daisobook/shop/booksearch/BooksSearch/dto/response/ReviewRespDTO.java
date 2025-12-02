package com.daisobook.shop.booksearch.BooksSearch.dto.response;

import java.time.ZonedDateTime;
import java.util.List;

public record ReviewRespDTO(
        long id,
        long bookId,
        String bookTitle,
        long userId,
        String content,
        int rating,
        ZonedDateTime createdAt,
        ZonedDateTime modifiedAt,
        List<ImageRespDTO> imageRespDTOList
) { }