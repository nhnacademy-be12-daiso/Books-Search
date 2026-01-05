package com.daisobook.shop.booksearch.dto.response.book;

import com.daisobook.shop.booksearch.dto.response.SortBookListRespDTO;

public record MainPageBookListRespDTO(
        SortBookListRespDTO bookOfTheWeek,
        SortBookListRespDTO newReleases
) {
}
