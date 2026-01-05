package com.daisobook.shop.booksearch.books_search.mapper.book;

import com.daisobook.shop.booksearch.books_search.dto.DiscountDTO;
import com.daisobook.shop.booksearch.books_search.dto.BookListData;
import com.daisobook.shop.booksearch.books_search.dto.BookUpdateData;
import com.daisobook.shop.booksearch.books_search.dto.projection.*;
import com.daisobook.shop.booksearch.books_search.dto.request.book.BookGroupReqV2DTO;
import com.daisobook.shop.booksearch.books_search.dto.request.book.BookReqV2DTO;
import com.daisobook.shop.booksearch.books_search.dto.response.book.BookAdminResponseDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.book.BookListRespDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.book.BookRespDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.book.BookUpdateView;
import com.daisobook.shop.booksearch.books_search.dto.response.order.OrderBookSummeryDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.order.OrderBooksInfoRespDTO;
import com.daisobook.shop.booksearch.books_search.entity.book.Book;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.domain.Page;
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
    Page<BookListRespDTO> toBookRespDTOPage(Page<BookListData> bookListDataPage, Map<Long, DiscountDTO.Response> discountPriceMap, Set<Long> likeSetBookId);
    Map<Long, BookListData> toBookListDataMap(List<BookListProjection> bookListProjectionList) throws JsonProcessingException;
    Page<BookListData> toBookListDataPage(Page<BookListProjection> bookListProjectionPage) throws JsonProcessingException;
    Map<Long, DiscountDTO.Request> toDiscountDTOMapByBookListData(Map<Long, BookListData> bookListDataMap);
    Map<Long, DiscountDTO.Request> toDiscountDTOMapByBookListData(Page<BookListData> bookListDataPage);
    Map<Long, DiscountDTO.Request> toDiscountDTOMapByBookInfoListProjection(List<BookInfoListProjection> bookInfoListDataMap);
    Map<Long, DiscountDTO.Request> toDiscountDTOMapByBookAdminProjection(Page<BookAdminProjection> bookAdminProjectionPage);
    List<OrderBookSummeryDTO> toOrderBookSummeryDTOList(List<BookSummeryProjection> bookSummeryProjections);
    BookUpdateView toBookUpdateView(BookUpdateViewProjection detail) throws JsonProcessingException;
    Page<BookAdminResponseDTO> toBookAdminResopnseDTOPage(Page<BookAdminProjection> adminProjectionPage, Map<Long, DiscountDTO.Response> discountPriceMap) throws JsonProcessingException;
}
