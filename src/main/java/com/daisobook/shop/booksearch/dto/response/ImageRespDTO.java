package com.daisobook.shop.booksearch.dto.response;

import com.daisobook.shop.booksearch.entity.ImageType;

public record ImageRespDTO(
        long no,
        String path,
        ImageType imageType) {
}
