package com.daisobook.shop.booksearch.dto.response.book;

import org.springframework.data.domain.Page;

public record BookListByCategoryRespDTO (
        Page<BookListRespDTO> bookList
){
}
