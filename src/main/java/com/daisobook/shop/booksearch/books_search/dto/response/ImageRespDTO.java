package com.daisobook.shop.booksearch.books_search.dto.response;

import com.daisobook.shop.booksearch.books_search.entity.ImageType;

public record ImageRespDTO(
        long no,
        String path,
        ImageType imageType) {
}
