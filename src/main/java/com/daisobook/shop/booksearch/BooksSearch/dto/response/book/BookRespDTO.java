package com.daisobook.shop.booksearch.BooksSearch.dto.response.book;

import com.daisobook.shop.booksearch.BooksSearch.dto.response.*;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.category.CategoryRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Status;

import java.math.BigDecimal;
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
        Long price,
        BigDecimal discountPercentage,
        Long discountPrice,
        boolean isPackaging,
        Integer stock,
        Status status,
//        String imageUrl,
        List<ImageRespDTO> imageList,
        Integer volumeNo,
        List<CategoryRespDTO> categories,
        List<TagRespDTO> tags,
        int likeCount,
        boolean isLike,
        List<ReviewRespDTO> reviewList){
}
