package com.daisobook.shop.booksearch.dto.request;

import com.daisobook.shop.booksearch.entity.ImageType;

public record ImageMetadataReqDTO (
        int sequence,
        ImageType type,
        String dataUrl,
        String fileKey) {
}
