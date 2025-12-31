package com.daisobook.shop.booksearch.BooksSearch.exception.custom.category;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.EntityNotFoundException;

public class NotFoundBookCategory extends EntityNotFoundException {
    public NotFoundBookCategory(String message) {
        super(message);
    }
}
