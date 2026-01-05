package com.daisobook.shop.booksearch.books_search.dto.response.book;

import org.springframework.data.domain.Page;

public record BookListByCategoryRespDTO (
        Page<BookListRespDTO> bookList
){
}
