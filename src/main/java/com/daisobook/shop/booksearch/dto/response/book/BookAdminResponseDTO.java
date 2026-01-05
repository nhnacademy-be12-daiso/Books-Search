package com.daisobook.shop.booksearch.dto.response.book;

import com.daisobook.shop.booksearch.dto.response.ImageRespDTO;
import com.daisobook.shop.booksearch.entity.book.Status;

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
