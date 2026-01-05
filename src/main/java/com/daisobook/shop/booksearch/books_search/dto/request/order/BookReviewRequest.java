package com.daisobook.shop.booksearch.books_search.dto.request.order;

import java.util.List;

public record BookReviewRequest(
        Long userId,
        List<BookOrderDetailRequest> bookOrderDetailRequests) {
}