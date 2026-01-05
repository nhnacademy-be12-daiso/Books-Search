package com.daisobook.shop.booksearch.dto.projection;

import com.daisobook.shop.booksearch.entity.book.Status;

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