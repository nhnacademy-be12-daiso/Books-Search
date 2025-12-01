package com.daisobook.shop.booksearch.BooksSearch.dto.response;

import com.daisobook.shop.booksearch.BooksSearch.entity.Status;

import java.time.LocalDate;
import java.util.List;

public record BookListRespDTO(
        long bookId,
        String title,
        List<AuthorRespDTO> authorList,
        String publisher,
        LocalDate publicationDate,
        int price,
        Status status,
        List<ImageRespDTO> imageList,
        Integer volumeNo){
}
