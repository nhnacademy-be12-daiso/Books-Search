package com.daisobook.shop.booksearch.books_search.dto.api;

import com.daisobook.shop.booksearch.books_search.dto.TagInfoData;
import com.daisobook.shop.booksearch.books_search.dto.response.AuthorInfoData;
import com.daisobook.shop.booksearch.books_search.dto.response.ImageRespDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.category.CategoryRespDTO;

import java.time.LocalDate;
import java.util.List;

public record BookInfoDataView(
        String isbn,
        String title,
        String index,
        String description,
        List<AuthorInfoData> authorList,
        String publisher,
        LocalDate publicationDate,
        Long price,
        List<ImageRespDTO> imageList,
        Integer volumeNo,
        List<CategoryRespDTO> categories,
        List<TagInfoData> tags) {
}
