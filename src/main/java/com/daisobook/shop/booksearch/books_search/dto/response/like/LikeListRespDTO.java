package com.daisobook.shop.booksearch.books_search.dto.response.like;

import com.daisobook.shop.booksearch.books_search.dto.response.AuthorRespDTO;
import com.daisobook.shop.booksearch.books_search.entity.book.Status;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

public record LikeListRespDTO(
        long likeId,
        long bookId,
        String isbn,
        String title,
        List<AuthorRespDTO> authorRespDTOList,
        Long price,
        BigDecimal discountPercentage,
        Long discountPrice,
        Status status,
        String image,
        Integer volumeNo,
        boolean packaging,
        ZonedDateTime createdAt
) {
}
