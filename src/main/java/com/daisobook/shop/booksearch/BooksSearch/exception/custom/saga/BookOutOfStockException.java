package com.daisobook.shop.booksearch.BooksSearch.exception.custom.saga;

public class BookOutOfStockException extends RuntimeException {
    public BookOutOfStockException(String message) {
        super(message);
    }
}
