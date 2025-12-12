package com.daisobook.shop.booksearch.BooksSearch.dto.response.order;

import java.math.BigDecimal;

public record OrderBookInfoRespDTO (
        long bookId,
        String title,
        Long Price,
        Integer stock,
        BigDecimal discountPercentage,
        Long discountPrice,
        String coverImage
) {
}
