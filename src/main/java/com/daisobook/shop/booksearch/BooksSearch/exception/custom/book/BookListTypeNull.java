package com.daisobook.shop.booksearch.BooksSearch.exception.custom.book;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.InvalidRequestException;

public class BookListTypeNull extends InvalidRequestException {
    public BookListTypeNull(String message) {
        super(message);
    }
}
