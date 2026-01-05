package com.daisobook.shop.booksearch.books_search.dto.request;

import com.daisobook.shop.booksearch.books_search.entity.ImageType;

public record ImageMetadataReqDTO (
        int sequence,
        ImageType type,
        String dataUrl,
        String fileKey) {
}
