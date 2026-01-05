package com.daisobook.shop.booksearch.dto.projection;

public interface BookReviewProjection {
    Long getBookId();
    String getTitle();
    String getImages();
    Long getOrderDetailId();
    Long getReviewId();
}
