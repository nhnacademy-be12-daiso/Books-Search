package com.daisobook.shop.booksearch.dto.projection;

public interface CategoryListProjection {
    long getCategoryId();
    String getCategoryName();
    Long getPreCategoryId();
    int getDeep();
}
