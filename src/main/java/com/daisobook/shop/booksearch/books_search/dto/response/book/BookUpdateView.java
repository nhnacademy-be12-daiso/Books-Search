package com.daisobook.shop.booksearch.books_search.dto.response.book;

import com.daisobook.shop.booksearch.books_search.dto.response.AuthorRespDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.ImageRespDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.TagRespDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.category.CategoryRespDTO;
import com.daisobook.shop.booksearch.books_search.entity.book.Status;

import java.time.LocalDate;
import java.util.List;

public record BookUpdateView (
        long bookId,
        String isbn,
        String title,
        String index,
        String description,
        List<AuthorRespDTO> authorList,
        String publisher,
        LocalDate publicationDate,
        Long price,
        boolean isPackaging,
        Integer stock,
        Status status,
        List<ImageRespDTO> imageList,
        Integer volumeNo,
        List<CategoryRespDTO> categories,
        List<TagRespDTO> tags,
        boolean isDeleted) {
}
