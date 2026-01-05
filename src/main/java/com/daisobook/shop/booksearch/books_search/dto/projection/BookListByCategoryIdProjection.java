package com.daisobook.shop.booksearch.books_search.dto.projection;

import com.daisobook.shop.booksearch.books_search.entity.book.Status;

public interface BookListByCategoryIdProjection {
    long getBookId();
    String getIsbn();
    String getTitle();
    String getDescription();
    String getAuthors();
    String getPublisher();
    String getPublicationDate();
    Long getPrice();
    Status getStatus();
    String getImages();
    String getCategories();
    String getTags();
    String getVolumeNo();
    boolean getIsPackaging();
}
