package com.daisobook.shop.booksearch.books_search.exception.custom.book;

import com.daisobook.shop.booksearch.books_search.exception.custom.InvalidRequestException;

public class BookListTypeNull extends InvalidRequestException {
    public BookListTypeNull(String message) {
        super(message);
    }
}
