package com.daisobook.shop.booksearch.BooksSearch.dto.response;

import java.util.List;
import java.util.Map;

public record HomeBookListRespDTO (
    Map<String, List<BookListRespDTO>> BookListMap){
}
