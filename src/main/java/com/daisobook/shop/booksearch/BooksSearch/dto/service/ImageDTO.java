package com.daisobook.shop.booksearch.BooksSearch.dto.service;

import com.daisobook.shop.booksearch.BooksSearch.entity.ImageType;

public record ImageDTO(
        long imageId,
        long connectedId,
        int no,
        String path,
        ImageType imageType) {
}
