package com.daisobook.shop.booksearch.books_search.dto.projection;

public interface CategoryPathProjection {
    Long getId();
    Long getPreCategoryId();
    Integer getDeep();
}
