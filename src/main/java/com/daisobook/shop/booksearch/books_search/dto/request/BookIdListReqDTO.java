package com.daisobook.shop.booksearch.books_search.dto.request;

import java.util.List;

public record BookIdListReqDTO (
        List<Long> bookIdList) {
}
