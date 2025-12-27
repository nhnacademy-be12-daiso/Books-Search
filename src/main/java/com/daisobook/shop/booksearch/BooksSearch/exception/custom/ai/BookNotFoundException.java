package com.daisobook.shop.booksearch.BooksSearch.exception.custom.ai;

public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(String isbn) {
        super("해당 ISBN[" + isbn + "]으로 검색된 도서 정보가 없습니다.");
    }
}