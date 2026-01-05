package com.daisobook.shop.booksearch.books_search.dto.request.book;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public record BookGroupReqV2DTO(
        BookReqV2DTO bookReqDTO,
        Map<String, MultipartFile> fileMap
) {
}
