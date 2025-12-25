package com.daisobook.shop.booksearch.BooksSearch.dto.projection;

import com.daisobook.shop.booksearch.BooksSearch.entity.book.Status;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.ZonedDateTime;

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