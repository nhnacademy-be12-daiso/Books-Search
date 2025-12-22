package com.daisobook.shop.booksearch.BooksSearch.mapper.book;

import com.daisobook.shop.booksearch.BooksSearch.dto.BookIdAndPriceData;
import com.daisobook.shop.booksearch.BooksSearch.dto.BookListData;
import com.daisobook.shop.booksearch.BooksSearch.dto.BookUpdateData;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.BookDetailProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.BookListProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookGroupReqV2DTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookReqV2DTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.book.BookListRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.book.BookRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.order.OrderBooksInfoRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface BookMapper {
    BookGroupReqV2DTO parsing(String metadataJson, MultipartFile image0, MultipartFile image1,
                              MultipartFile image2, MultipartFile image3, MultipartFile image4) throws JsonProcessingException;
    Book create(BookReqV2DTO req);
    BookUpdateData toBookUpdateData(BookReqV2DTO req);
    OrderBooksInfoRespDTO toOrderBookInfoRespDTOList(List<Book> bookList, Map<Long, Long> discountPriceMap);
    BookRespDTO toBookRespDTO(BookDetailProjection bookDetail, Integer likeCount, Boolean likeCheck, Long discountPrice) throws JsonProcessingException;
    List<BookListRespDTO> toBookRespDTOList(Map<Long, BookListData> bookListDataMap, Set<Long> likeSetBookId);
    Map<Long, BookListData> toBookListDataMap(List<BookListProjection> bookListProjectionList) throws JsonProcessingException;
    Map<Long, BookIdAndPriceData> toBookIdAndPriceDataMap(Map<Long, BookListData> bookListDataMap);
}
