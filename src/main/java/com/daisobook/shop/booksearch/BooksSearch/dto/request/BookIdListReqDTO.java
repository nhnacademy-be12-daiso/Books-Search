package com.daisobook.shop.booksearch.BooksSearch.dto.request;

import java.util.List;

public record BookIdListReqDTO (
        List<Long> bookIdList) {
}
