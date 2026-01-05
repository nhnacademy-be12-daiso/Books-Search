package com.daisobook.shop.booksearch.dto;

import java.math.BigDecimal;

public class DiscountDTO {
    public record Request(
            Long bookId,
            Long price
    ){}

    public record Response(
            Long bookId,
            Long price,
            BigDecimal discountPercentage,
            Long discountPrice
    ){}
}