package com.daisobook.shop.booksearch.books_search.dto.request.order;

public record OrderCancelRequest (
        long bookId,
        int quantity
) {
}
