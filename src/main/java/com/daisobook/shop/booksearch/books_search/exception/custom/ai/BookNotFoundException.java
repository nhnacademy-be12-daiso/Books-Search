package com.daisobook.shop.booksearch.books_search.exception.custom.ai;

import com.daisobook.shop.booksearch.books_search.exception.custom.BusinessException;
import org.springframework.http.HttpStatus;

public class BookNotFoundException extends BusinessException {
    public BookNotFoundException(String isbn) {
        super("해당 ISBN[" + isbn + "]으로 검색된 도서 정보가 없습니다.", HttpStatus.NOT_FOUND);
    }
}