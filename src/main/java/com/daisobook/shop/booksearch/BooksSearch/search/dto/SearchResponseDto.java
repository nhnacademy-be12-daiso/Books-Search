package com.daisobook.shop.booksearch.BooksSearch.search.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

// 도서 검색 응답 DTO
@Getter
@Builder
public class SearchResponseDto {
    private List<BookResponseDto> bookList;

    public static SearchResponseDto empty() {
        return SearchResponseDto.builder()
                .bookList(Collections.emptyList())
                .build();
    }
}