package com.daisobook.shop.booksearch.BooksSearch.search.message;

import org.springframework.beans.factory.annotation.Value;

public final class BookSearchMqConstants {
    private BookSearchMqConstants() {}

    public static final String EXCHANGE = "team3.booksearch.exchange";

    public static final String RK_AI_ANALYSIS = "team3.booksearch.ai.analysis";
    public static final String RK_BOOK_UPSERT = "team3.booksearch.book.upsert";
    public static final String RK_BOOK_DELETE = "team3.booksearch.book.delete";


}
