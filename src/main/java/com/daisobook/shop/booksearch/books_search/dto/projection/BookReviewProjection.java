package com.daisobook.shop.booksearch.books_search.dto.projection;

public interface BookReviewProjection {
    Long getBookId();
    String getTitle();
    String getImages();
    Long getOrderDetailId();
    Long getReviewId();
}
