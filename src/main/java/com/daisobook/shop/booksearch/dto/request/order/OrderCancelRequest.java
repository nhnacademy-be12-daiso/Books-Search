package com.daisobook.shop.booksearch.dto.request.order;

public record OrderCancelRequest (
        long bookId,
        int quantity
) {
}
