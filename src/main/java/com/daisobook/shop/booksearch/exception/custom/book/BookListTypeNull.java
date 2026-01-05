package com.daisobook.shop.booksearch.exception.custom.book;

import com.daisobook.shop.booksearch.exception.custom.InvalidRequestException;

public class BookListTypeNull extends InvalidRequestException {
    public BookListTypeNull(String message) {
        super(message);
    }
}
