package com.daisobook.shop.booksearch.BooksSearch.dto.api;

import com.daisobook.shop.booksearch.BooksSearch.dto.TagInfoData;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.AuthorInfoData;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.ImageRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.category.CategoryRespDTO;

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
