package com.daisobook.shop.booksearch.BooksSearch.dto.request;

import com.daisobook.shop.booksearch.BooksSearch.entity.ImageType;

public record ImageMetadataReqDTO (
        int sequence,
        ImageType type,
        String dataUrl,
        String fileKey) {
}
