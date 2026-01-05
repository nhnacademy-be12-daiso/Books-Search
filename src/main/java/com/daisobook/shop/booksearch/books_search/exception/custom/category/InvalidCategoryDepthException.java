package com.daisobook.shop.booksearch.books_search.exception.custom.category;

import com.daisobook.shop.booksearch.books_search.exception.custom.InvalidRequestException;

public class InvalidCategoryDepthException extends InvalidRequestException {
    public InvalidCategoryDepthException(String message) {
        super(message);
    }
}
