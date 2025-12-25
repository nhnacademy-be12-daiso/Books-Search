package com.daisobook.shop.booksearch.BooksSearch.dto.projection;

import com.daisobook.shop.booksearch.BooksSearch.entity.book.Status;

public interface BookInfoListProjection {
    long getBookId();
    String getTitle();
    Long getPrice();
    Integer getStock();
    Status getStatus();
    String getCoverImage();
    Integer getVolumeNo();
    boolean getIsPackaging();
}