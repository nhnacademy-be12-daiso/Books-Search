package com.daisobook.shop.booksearch.books_search.dto.response.order;

import java.util.List;

public record OrderBooksInfoRespDTO (
        List<OrderBookInfoRespDTO> orderBookInfoRespDTOList
){
}
