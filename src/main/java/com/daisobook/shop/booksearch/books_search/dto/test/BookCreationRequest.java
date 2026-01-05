package com.daisobook.shop.booksearch.books_search.dto.test;

import com.daisobook.shop.booksearch.books_search.dto.request.ImageMetadataReqDTO;

import java.util.List;

public record BookCreationRequest (
        long bookId,
        String title,
        // ... 기타 도서 정보 필드
        List<ImageMetadataReqDTO> imageMetadata // 핵심: 이미지 메타데이터 리스트
        ){
}