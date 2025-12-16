package com.daisobook.shop.booksearch.BooksSearch.dto;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.AuthorReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.ImageMetadataReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Status;

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
//        List<ImageMetadataReqDTO> imageList,
        Integer volumeNo,
        Long category,
        List<String> tag,
        Boolean isDeleted){
}
