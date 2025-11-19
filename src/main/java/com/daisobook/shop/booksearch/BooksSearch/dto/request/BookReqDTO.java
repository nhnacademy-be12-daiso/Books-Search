package com.daisobook.shop.booksearch.BooksSearch.dto.request;

import com.daisobook.shop.booksearch.BooksSearch.entity.Status;

import java.time.LocalDate;
import java.util.List;

public record BookReqDTO(
        String isbn,
        String title,
        String index,
        String description,
        String author,
        String publisher,
        LocalDate publicationDate,
        Integer price,
        boolean isPackaging,
        Integer stock,
        Status status,
        String imageUrl,
        Integer volumeNo,
        List<CategoryReqDTO> categories,
        List<TagReqDTO> tags){
}
