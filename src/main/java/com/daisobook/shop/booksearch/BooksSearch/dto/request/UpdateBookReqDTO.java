package com.daisobook.shop.booksearch.BooksSearch.dto.request;

import com.daisobook.shop.booksearch.BooksSearch.entity.Status;

import java.time.LocalDate;
import java.util.List;

public record UpdateBookReqDTO (
        long id,
        String isbn,
        String title,
        String index,
        String description,
        String author,
        String publisher,
        LocalDate publicationDate,
        int price,
        boolean isPackaging,
        Integer stock,
        Status status,
        List<UpdateCategoryReqDTO> categories,
        List<UpdateTagReqDTO> tags){
}
