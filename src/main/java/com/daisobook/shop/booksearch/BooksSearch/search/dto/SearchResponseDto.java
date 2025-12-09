package com.daisobook.shop.booksearch.BooksSearch.search.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

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