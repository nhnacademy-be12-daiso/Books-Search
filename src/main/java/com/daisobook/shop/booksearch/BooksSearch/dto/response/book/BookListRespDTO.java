package com.daisobook.shop.booksearch.BooksSearch.dto.response.book;

import com.daisobook.shop.booksearch.BooksSearch.dto.response.AuthorRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.category.CategoryRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.ImageRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.TagRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record BookListRespDTO(
        long bookId,
        String isbn,
        String title,
        String description,
        List<AuthorRespDTO> authorList,
        String publisher,
        LocalDate publicationDate,
        Long price,
        BigDecimal discountPercentage,
        Long discountPrice,
        Status status,
        List<ImageRespDTO> imageList,
        List<CategoryRespDTO> categories,
        List<TagRespDTO> tags,
        Integer volumeNo,
        boolean isPackaging,
        Boolean isLike,
        boolean isDeleted){
}
