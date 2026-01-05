package com.daisobook.shop.booksearch.books_search.dto.response.book;

import com.daisobook.shop.booksearch.books_search.dto.response.SortBookListRespDTO;

public record MainPageBookListRespDTO(
        SortBookListRespDTO bookOfTheWeek,
        SortBookListRespDTO newReleases
) {
}
