package com.daisobook.shop.booksearch.dto.projection;

import com.daisobook.shop.booksearch.entity.book.Status;

import java.time.LocalDate;

public interface BookAdminProjection {
    long getBookId();
    String getIsbn();
    String getTitle();
    String getImages();
    Long getPrice();
    int getStock();
    Status getStatus();
    LocalDate getPublicationDate();
    String getPublisher();
    boolean getIsDeleted();
}
