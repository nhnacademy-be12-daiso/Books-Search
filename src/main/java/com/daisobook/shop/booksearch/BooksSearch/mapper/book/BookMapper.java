package com.daisobook.shop.booksearch.BooksSearch.mapper.book;

import com.daisobook.shop.booksearch.BooksSearch.dto.response.order.OrderBooksInfoRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;

import java.util.List;
import java.util.Map;

public interface BookMapper {
    OrderBooksInfoRespDTO toOrderBooksInfoRespDTOList(List<Book> bookList, Map<Long, Long> discountPriceMap);
}
