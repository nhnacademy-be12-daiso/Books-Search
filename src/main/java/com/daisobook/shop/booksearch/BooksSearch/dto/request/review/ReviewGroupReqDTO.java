package com.daisobook.shop.booksearch.BooksSearch.dto.request.review;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public record ReviewGroupReqDTO(
        ReviewReqDTO reviewReqDTO,
        Map<String, MultipartFile> fileMap) {
}
