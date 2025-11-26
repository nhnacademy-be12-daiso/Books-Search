package com.daisobook.shop.booksearch.BooksSearch.dto.request.book;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.AuthorReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.CategoryReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.ImageMetadataReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.TagReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.Status;

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
        Integer price,
        boolean isPackaging,
        Integer stock,
        Status status,
//        String imageUrl,
        List<ImageMetadataReqDTO> imageMetadataReqDTOList,
        Integer volumeNo,
        List<CategoryReqDTO> categories,
        List<TagReqDTO> tags){
}
