package com.daisobook.shop.booksearch.BooksSearch.dto.request.book;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public record BookGroupReqDTO(
        BookReqDTO bookReqDTO,
        Map<String, MultipartFile> fileMap) {
}
