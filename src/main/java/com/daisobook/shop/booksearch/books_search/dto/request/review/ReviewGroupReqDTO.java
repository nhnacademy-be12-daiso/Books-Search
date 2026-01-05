package com.daisobook.shop.booksearch.books_search.dto.request.review;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public record ReviewGroupReqDTO(
        ReviewReqDTO reviewReqDTO,
        Map<String, MultipartFile> fileMap) {
}
