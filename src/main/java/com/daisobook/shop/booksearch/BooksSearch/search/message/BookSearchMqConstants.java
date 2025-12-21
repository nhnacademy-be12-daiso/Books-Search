package com.daisobook.shop.booksearch.BooksSearch.search.message;

import org.springframework.beans.factory.annotation.Value;

public final class BookSearchMqConstants {

    @Value("${rabbitmq.exchange.main}")
    public static String EXCHANGE;

    @Value("${rabbitmq.routing-key.ai-analysis}")
    public static String RK_AI_ANALYSIS;
    @Value("${rabbitmq.routing-key.book-upsert}")
    public static String RK_BOOK_UPSERT;
    @Value("${rabbitmq.routing-key.book-delete}")
    public static String RK_BOOK_DELETE;
}
