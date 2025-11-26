package com.daisobook.shop.booksearch.BooksSearch.dto.request.book;

import org.springframework.web.multipart.MultipartFile;

public record BookMetadataReqDTO(
        String metadata,
        MultipartFile image0,
        MultipartFile image1,
        MultipartFile image2,
        MultipartFile image3,
        MultipartFile image4) {

}
