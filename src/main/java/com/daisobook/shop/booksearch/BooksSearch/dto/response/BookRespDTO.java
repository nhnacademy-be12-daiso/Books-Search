package com.daisobook.shop.booksearch.BooksSearch.dto.response;

import com.daisobook.shop.booksearch.BooksSearch.entity.Status;

import java.time.LocalDate;
import java.util.List;

public record BookRespDTO(
        long bookId,
        String isbn,
        String title,
        String index,
        String description,
        List<AuthorRespDTO> authorList,
        String publisher,
        LocalDate publicationDate,
        int price,
        boolean isPackaging,
        Integer stock,
        Status status,
//        String imageUrl,
        List<ImageRespDTO> imageList,
        Integer volumeNo,
        List<CategoryRespDTO> categories,
        List<TagRespDTO> tags){
}
