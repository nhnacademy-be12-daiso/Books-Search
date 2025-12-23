package com.daisobook.shop.booksearch.BooksSearch.mapper.book;

import com.daisobook.shop.booksearch.BooksSearch.dto.DiscountDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.BookListData;
import com.daisobook.shop.booksearch.BooksSearch.dto.BookUpdateData;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.BookDetailProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.BookInfoListProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.BookListProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.BookSummeryProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookGroupReqV2DTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookReqV2DTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.book.BookListRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.book.BookRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.order.OrderBookSummeryDTO;
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
    OrderBooksInfoRespDTO toOrderBookInfoRespDTOList(Map<Long, DiscountDTO.Response> discountDTOMap, List<BookInfoListProjection> bookInfoListProjections);
    BookRespDTO toBookRespDTO(BookDetailProjection bookDetail, Integer likeCount, Boolean likeCheck, Long discountPrice) throws JsonProcessingException;
    List<BookListRespDTO> toBookRespDTOList(Map<Long, BookListData> bookListDataMap, Map<Long, DiscountDTO.Response> discountPriceMap, Set<Long> likeSetBookId);
    Map<Long, BookListData> toBookListDataMap(List<BookListProjection> bookListProjectionList) throws JsonProcessingException;
    Map<Long, DiscountDTO.Request> toDiscountDTOMap(Map<Long, BookListData> bookListDataMap);
    Map<Long, DiscountDTO.Request> toDiscountDTOMap(List<BookInfoListProjection> bookInfoListDataMap);
    List<OrderBookSummeryDTO> toOrderBookSummeryDTOList(List<BookSummeryProjection> bookSummeryProjections);
}
