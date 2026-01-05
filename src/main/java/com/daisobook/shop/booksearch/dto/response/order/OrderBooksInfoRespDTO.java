package com.daisobook.shop.booksearch.dto.response.order;

import java.util.List;

public record OrderBooksInfoRespDTO (
        List<OrderBookInfoRespDTO> orderBookInfoRespDTOList
){
}
