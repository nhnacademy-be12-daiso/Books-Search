package com.daisobook.shop.booksearch.books_search.dto.response.order;

import com.daisobook.shop.booksearch.books_search.entity.book.Status;

import java.math.BigDecimal;

public record OrderBookInfoRespDTO (
        long bookId,
        String title,
        Long price,
        Integer stock,
        Status status,
        BigDecimal discountPercentage,
        Long discountPrice,
        String coverImage,
        Integer volumeNo,
        boolean isPackaging
) {
}
