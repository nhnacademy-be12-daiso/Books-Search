package com.daisobook.shop.booksearch.BooksSearch.entity;

public enum BookListType {
    NEW_RELEASES("newReleases"), BOOK_OF_THE_WEEK("bookOfTheWeek");

    private final String value;

    BookListType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
