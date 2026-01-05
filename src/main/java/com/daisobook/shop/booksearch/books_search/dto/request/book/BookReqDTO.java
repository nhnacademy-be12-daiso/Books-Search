package com.daisobook.shop.booksearch.books_search.dto.request.book;

import com.daisobook.shop.booksearch.books_search.dto.request.AuthorReqDTO;
import com.daisobook.shop.booksearch.books_search.dto.request.category.CategoryReqDTO;
import com.daisobook.shop.booksearch.books_search.dto.request.ImageMetadataReqDTO;
import com.daisobook.shop.booksearch.books_search.dto.request.TagReqDTO;
import com.daisobook.shop.booksearch.books_search.entity.book.Status;

import java.time.LocalDate;
import java.util.List;

public record BookReqDTO(
        String isbn,
        String title,
        String index,
        String description,
//        String author,
        List<AuthorReqDTO> authorReqDTOList,
        String publisher,
        LocalDate publicationDate,
        Long price,
        boolean isPackaging,
        Integer stock,
        Status status,
//        String imageUrl,
        List<ImageMetadataReqDTO> imageMetadataReqDTOList,
        Integer volumeNo,
        List<CategoryReqDTO> categories,
        List<TagReqDTO> tags){
}
