package com.daisobook.shop.booksearch.BooksSearch.mapper.book.impl;

import com.daisobook.shop.booksearch.BooksSearch.dto.response.order.OrderBookInfoRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.order.OrderBooksInfoRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.mapper.book.BookMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class BookMapperImpl implements BookMapper {

    @Override
    public OrderBooksInfoRespDTO toOrderBooksInfoRespDTOList(List<Book> bookList, Map<Long, Long> discountPriceMap){

        return new OrderBooksInfoRespDTO(bookList.stream()
                .map(b ->
                        new OrderBookInfoRespDTO(b.getId(), b.getTitle(), b.getPrice(), b.getStock(),
                                discountPriceMap.containsKey(b.getId()) ? BigDecimal.valueOf((double) discountPriceMap.get(b.getId()) / b.getPrice() * 100.0) : null,
                                discountPriceMap.getOrDefault(b.getId(), null)))
                .toList());
    }
}
