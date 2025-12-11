package com.daisobook.shop.booksearch.BooksSearch.dto.response.order;

import java.util.List;

public record OrderBooksInfoRespDTO (
        List<OrderBookInfoRespDTO> orderBookInfoRespDTOList
){
}
