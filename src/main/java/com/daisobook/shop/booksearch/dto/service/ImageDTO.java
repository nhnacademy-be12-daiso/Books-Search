package com.daisobook.shop.booksearch.dto.service;

import com.daisobook.shop.booksearch.entity.ImageType;

public record ImageDTO(
        long imageId,
        long connectedId,
        int no,
        String path,
        ImageType imageType) {
}
