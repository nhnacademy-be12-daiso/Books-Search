package com.daisobook.shop.booksearch.BooksSearch.dto.projection;

public interface CategoryPathProjection {
    Long getId();
    Long getPreCategoryId();
    Integer getDeep();
}
