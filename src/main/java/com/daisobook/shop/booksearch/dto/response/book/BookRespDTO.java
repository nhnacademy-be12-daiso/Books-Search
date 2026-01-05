package com.daisobook.shop.booksearch.dto.response.book;

import com.daisobook.shop.booksearch.dto.response.AuthorRespDTO;
import com.daisobook.shop.booksearch.dto.response.ImageRespDTO;
import com.daisobook.shop.booksearch.dto.response.ReviewRespDTO;
import com.daisobook.shop.booksearch.dto.response.TagRespDTO;
import com.daisobook.shop.booksearch.dto.response.category.CategoryRespDTO;
import com.daisobook.shop.booksearch.entity.book.Status;

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
        List<ReviewRespDTO> reviewList,
        boolean isDeleted){
}
