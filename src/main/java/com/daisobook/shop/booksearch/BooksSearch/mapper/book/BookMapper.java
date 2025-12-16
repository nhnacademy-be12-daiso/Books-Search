package com.daisobook.shop.booksearch.BooksSearch.mapper.book;

import com.daisobook.shop.booksearch.BooksSearch.dto.BookUpdateData;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookGroupReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookGroupReqV2DTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookMetadataReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookReqV2DTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.order.OrderBooksInfoRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface BookMapper {
    BookGroupReqV2DTO parsing(String metadataJson, MultipartFile image0, MultipartFile image1,
                              MultipartFile image2, MultipartFile image3, MultipartFile image4) throws JsonProcessingException;
    Book create(BookReqV2DTO req);
    BookUpdateData toBookUpdateData(BookReqV2DTO req);
    OrderBooksInfoRespDTO toOrderBookInfoRespDTOList(List<Book> bookList, Map<Long, Long> discountPriceMap);
}
