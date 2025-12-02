package com.daisobook.shop.booksearch.BooksSearch.exception.custom;

public class DuplicatedTag extends RuntimeException {
    public DuplicatedTag(String message) {
        super(message);
    }
}
