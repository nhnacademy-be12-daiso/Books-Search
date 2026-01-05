package com.daisobook.shop.booksearch.books_search.dto.response;

import com.daisobook.shop.booksearch.books_search.dto.response.book.BookListRespDTO;

import java.util.List;

public record SortBookListRespDTO(
        List<BookListRespDTO> bookListRespDTOS
) {
}
