package com.daisobook.shop.booksearch.BooksSearch.dto.response.order;

import com.daisobook.shop.booksearch.BooksSearch.entity.book.Status;

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
