package com.daisobook.shop.booksearch.books_search.dto.projection;

import com.daisobook.shop.booksearch.books_search.entity.policy.DiscountType;

public interface DiscountValueProjection {
    long getId();
    String getName();
    DiscountType getDiscountType();
    Double getValue();
}
