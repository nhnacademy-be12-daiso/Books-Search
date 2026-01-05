package com.daisobook.shop.booksearch.exception.custom.category;

import com.daisobook.shop.booksearch.exception.custom.DuplicateResourceException;

public class ExistedCategory extends DuplicateResourceException {
    public ExistedCategory(String message) {
        super(message);
    }
}
