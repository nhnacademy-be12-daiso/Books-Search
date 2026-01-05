package com.daisobook.shop.booksearch.exception.custom.category;

import com.daisobook.shop.booksearch.exception.custom.DuplicateResourceException;

public class DuplicatedCategory extends DuplicateResourceException {
    public DuplicatedCategory(String message) {
        super(message);
    }
}
