package com.daisobook.shop.booksearch.books_search.dto.projection;

public interface CategoryListProjection {
    long getCategoryId();
    String getCategoryName();
    Long getPreCategoryId();
    int getDeep();
}
