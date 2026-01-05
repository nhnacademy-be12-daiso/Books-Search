package com.daisobook.shop.booksearch.books_search.dto.projection;

import com.daisobook.shop.booksearch.books_search.entity.book.Status;

public interface LikeBookListProjection {
    long getLikeId();
    long getBookId();
    String getIsbn();
    String getTitle();
    String getAuthors();
    Long getPrice();
    Status getStatus();
    String getImage();
    Integer getVolumeNo();
    boolean getPackaging();
    String getCreatedAt();
}