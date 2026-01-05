package com.daisobook.shop.booksearch.dto.request.review;

import com.daisobook.shop.booksearch.dto.request.ImageMetadataReqDTO;

import java.time.ZonedDateTime;
import java.util.List;

public record ReviewReqDTO(
        long bookId,
        long userId,
        long orderDetailId,
        String content,
        int rating,
        ZonedDateTime createdAt,
        ZonedDateTime modifiedAt,
        List<ImageMetadataReqDTO> imageMetadataReqDTOList) {
}
