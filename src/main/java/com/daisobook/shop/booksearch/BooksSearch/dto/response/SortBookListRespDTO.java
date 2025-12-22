package com.daisobook.shop.booksearch.BooksSearch.dto.response;

import com.daisobook.shop.booksearch.BooksSearch.dto.response.book.BookListRespDTO;

import java.util.List;

public record SortBookListRespDTO(
        List<BookListRespDTO> bookListRespDTOS
) {
}
