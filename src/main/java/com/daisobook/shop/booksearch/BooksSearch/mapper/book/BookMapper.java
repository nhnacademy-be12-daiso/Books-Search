package com.daisobook.shop.booksearch.BooksSearch.mapper.book;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookGroupReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookMetadataReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.order.OrderBooksInfoRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;
import java.util.Map;

public interface BookMapper {
    BookGroupReqDTO parsing(BookMetadataReqDTO dto) throws JsonProcessingException;
    OrderBooksInfoRespDTO toOrderBookInfoRespDTOList(List<Book> bookList, Map<Long, Long> discountPriceMap);
}
