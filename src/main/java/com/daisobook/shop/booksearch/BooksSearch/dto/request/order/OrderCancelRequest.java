package com.daisobook.shop.booksearch.BooksSearch.dto.request.order;

public record OrderCancelRequest (
        long bookId,
        int quantity
) {
}
