package com.daisobook.shop.booksearch.dto.response;

import com.daisobook.shop.booksearch.dto.response.book.BookListRespDTO;

import java.util.List;

public record SortBookListRespDTO(
        List<BookListRespDTO> bookListRespDTOS
) {
}
