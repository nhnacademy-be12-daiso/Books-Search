package com.daisobook.shop.booksearch.BooksSearch.dto.request.book;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.AuthorReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.ImageMetadataReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.TagReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Status;

import java.time.LocalDate;
import java.util.List;

public record BookReqV2DTO (
        String isbn,
        String title,
        String index,
        String description,
        List<AuthorReqDTO> authorReqDTOList,
        String publisher,
        LocalDate publicationDate,
        Long price,
        boolean isPackaging,
        Integer stock,
        Status status,
        List<ImageMetadataReqDTO> imageMetadataReqDTOList,
        Integer volumeNo,
        Long categoryId,
        List<TagReqDTO> tags,
        boolean isDeleted){
}
