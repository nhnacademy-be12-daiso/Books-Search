package com.daisobook.shop.booksearch.BooksSearch.dto.response.meta;

import com.daisobook.shop.booksearch.BooksSearch.dto.response.TotalDataRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.book.BookAdminResponseDTO;
import org.springframework.data.domain.Page;

public record AdminBookMetaData (
        Page<BookAdminResponseDTO> bookAdminResponseDTOS,
        TotalDataRespDTO totalDate
) {
}
