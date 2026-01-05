package com.daisobook.shop.booksearch.dto.request.order;

import java.util.List;

public record BookReviewRequest(
        Long userId,
        List<BookOrderDetailRequest> bookOrderDetailRequests) {
}