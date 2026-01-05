package com.daisobook.shop.booksearch.exception.custom.category;

import com.daisobook.shop.booksearch.exception.custom.InvalidRequestException;

public class InvalidCategoryDepthException extends InvalidRequestException {
    public InvalidCategoryDepthException(String message) {
        super(message);
    }
}
