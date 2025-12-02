package com.daisobook.shop.booksearch.BooksSearch.exception.custom;

public class DuplicatedCategory extends RuntimeException {
    public DuplicatedCategory(String message) {
        super(message);
    }
}
