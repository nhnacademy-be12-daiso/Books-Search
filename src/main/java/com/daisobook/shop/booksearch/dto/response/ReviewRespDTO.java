package com.daisobook.shop.booksearch.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.ZonedDateTime;
import java.util.List;

public record ReviewRespDTO(
        long id,
        Long bookId,
        String bookTitle,
        long userId,
        String content,
        int rating,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX")
        ZonedDateTime createdAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX")
        ZonedDateTime modifiedAt,
        List<ImageRespDTO> reviewImages
) { }