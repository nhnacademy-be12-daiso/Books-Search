package com.daisobook.shop.booksearch.BooksSearch.dto.request.review;

import org.springframework.web.multipart.MultipartFile;

public record ReviewMetadataReqDTO(
        String metadata,
        MultipartFile image0,
        MultipartFile image1,
        MultipartFile image2) {
}
