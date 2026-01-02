package com.daisobook.shop.booksearch.BooksSearch.dto.response.book;

import com.daisobook.shop.booksearch.BooksSearch.dto.response.SortBookListRespDTO;

public record MainPageBookListRespDTO(
        SortBookListRespDTO bookOfTheWeek,
        SortBookListRespDTO newReleases
) {
}
