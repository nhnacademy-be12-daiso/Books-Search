package com.daisobook.shop.booksearch.BooksSearch.dto.projection;

public interface CategoryListProjection {
    long getCategoryId();
    String getCategoryName();
    Long getPreCategoryId();
    int getDeep();
}
