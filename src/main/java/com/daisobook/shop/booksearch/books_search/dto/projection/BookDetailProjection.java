package com.daisobook.shop.booksearch.books_search.dto.projection;

import com.daisobook.shop.booksearch.books_search.entity.book.Status;

import java.time.LocalDate;

public interface BookDetailProjection {
    long getId();
    String getIsbn();
    String getTitle();
    String getIndex();
    String getDescription();
    String getAuthors();
    String getPublisher();
    LocalDate getPublicationDate();
    Long getPrice();
    Integer getStock();
    Status getStatus();
    String getImages();
    String getCategories();
    String getTags();
    Integer getVolumeNo();
    boolean getIsPackaging();
    String getReviews();
    boolean getIsDeleted();
}
