package com.daisobook.shop.booksearch.books_search.dto.response.meta;

import com.daisobook.shop.booksearch.books_search.dto.response.TotalDataRespDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.book.BookAdminResponseDTO;
import org.springframework.data.domain.Page;

public record AdminBookMetaData (
        Page<BookAdminResponseDTO> bookAdminResponseDTOS,
        TotalDataRespDTO totalDate
) {
}
