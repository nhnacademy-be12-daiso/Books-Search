package com.daisobook.shop.booksearch.BooksSearch.dto.response;

import com.daisobook.shop.booksearch.BooksSearch.entity.ImageType;

public record ImageRespDTO(
        long no,
        String path,
        ImageType imageType) {
}
