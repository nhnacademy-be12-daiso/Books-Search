package com.daisobook.shop.booksearch.dto.projection;

import com.daisobook.shop.booksearch.entity.policy.DiscountType;

public interface DiscountValueProjection {
    long getId();
    String getName();
    DiscountType getDiscountType();
    Double getValue();
}
