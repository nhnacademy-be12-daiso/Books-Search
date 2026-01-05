package com.daisobook.shop.booksearch.dto.request.book;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public record BookGroupReqV2DTO(
        BookReqV2DTO bookReqDTO,
        Map<String, MultipartFile> fileMap
) {
}
