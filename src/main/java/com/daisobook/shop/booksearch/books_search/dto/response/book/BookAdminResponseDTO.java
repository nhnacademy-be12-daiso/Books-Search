package com.daisobook.shop.booksearch.books_search.dto.response.book;

import com.daisobook.shop.booksearch.books_search.dto.response.ImageRespDTO;
import com.daisobook.shop.booksearch.books_search.entity.book.Status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record BookAdminResponseDTO(
        long bookId,
        String isbn,
        String title,
        List<ImageRespDTO> imageList,
        Long price,
        BigDecimal discountPercentage,
        Long discountPrice,
        int stock,
        Status status,
        LocalDate publicationDate,
        String publisher,
        boolean isDeleted
) {
}
