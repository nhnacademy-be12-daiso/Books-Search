package com.daisobook.shop.booksearch.books_search.dto;

import com.daisobook.shop.booksearch.books_search.dto.request.AuthorReqDTO;
import com.daisobook.shop.booksearch.books_search.entity.book.Status;

import java.time.LocalDate;
import java.util.List;

public record BookUpdateData (
        String title,
        String index,
        String description,
        List<AuthorReqDTO> author,
        String publisher,
        LocalDate publicationDate,
        Long price,
        Boolean isPackaging,
        Integer stock,
        Status status,
        Integer volumeNo,
        Long category,
        List<String> tag,
        Boolean isDeleted){
}
