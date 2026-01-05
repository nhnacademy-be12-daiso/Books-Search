package com.daisobook.shop.booksearch.books_search.dto.service;

import com.daisobook.shop.booksearch.books_search.entity.ImageType;

public record ImageDTO(
        long imageId,
        long connectedId,
        int no,
        String path,
        ImageType imageType) {
}
