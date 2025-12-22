package com.daisobook.shop.booksearch.BooksSearch.dto.projection;

import com.daisobook.shop.booksearch.BooksSearch.entity.policy.DiscountType;

public interface DiscountValueProjection {
    long getId();
    String getName();
    DiscountType getDiscountType();
    Double getValue();
}
