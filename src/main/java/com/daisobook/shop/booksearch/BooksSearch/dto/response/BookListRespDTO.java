package com.daisobook.shop.booksearch.BooksSearch.dto.response;

import com.daisobook.shop.booksearch.BooksSearch.entity.book.Status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record BookListRespDTO(
        long bookId,
        String title,
        List<AuthorRespDTO> authorList,
        String publisher,
        LocalDate publicationDate,
        Long price,
        BigDecimal discountPercentage,
        Long discountPrice,
        Status status,
        List<ImageRespDTO> imageList,
        List<CategoryRespDTO> categories,
        Integer volumeNo,
        boolean isPackaging,
        Boolean isLike){
}
