package com.daisobook.shop.booksearch.dto.projection;

public interface CategoryPathProjection {
    Long getId();
    Long getPreCategoryId();
    Integer getDeep();
}
