package com.daisobook.shop.booksearch.dto.request;

import java.util.List;

public record BookIdListReqDTO (
        List<Long> bookIdList) {
}
