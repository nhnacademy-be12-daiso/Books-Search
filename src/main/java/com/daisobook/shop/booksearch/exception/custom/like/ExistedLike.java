package com.daisobook.shop.booksearch.exception.custom.like;

import com.daisobook.shop.booksearch.exception.custom.DuplicateResourceException;

public class ExistedLike extends DuplicateResourceException {
    public ExistedLike(String message) {
        super(message);
    }
}
