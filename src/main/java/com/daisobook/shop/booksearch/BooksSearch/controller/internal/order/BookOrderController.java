package com.daisobook.shop.booksearch.BooksSearch.controller.internal.order;

import com.daisobook.shop.booksearch.BooksSearch.controller.docs.BookOrderControllerDocs;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.BookIdListReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.order.BookReviewRequest;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.order.OrderCancelRequest;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.book.BookListRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.order.BookReviewResponse;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.order.OrderBookSummeryDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.order.OrderBooksInfoRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.service.book.impl.BookFacade;
import com.daisobook.shop.booksearch.BooksSearch.service.review.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/books/order-service")
public class BookOrderController implements BookOrderControllerDocs {
    private final BookFacade bookFacade;
    private final ReviewService reviewService;

    //POST: /api/v2/books/order-service/books/info
    @PostMapping("/books/info")
    public OrderBooksInfoRespDTO getOrderBookInfoList(@RequestBody BookIdListReqDTO bookIdListReqDTO){
        return bookFacade.findBooksByIdIn(bookIdListReqDTO.bookIdList());
    }

    //POST: /api/v2/books/order-service/books
    @PostMapping("/books/list")
    public List<OrderBookSummeryDTO> getBookList(@RequestBody BookIdListReqDTO bookIdListReqDTO){
        return bookFacade.getOrderBookList(bookIdListReqDTO.bookIdList());
    }

    //POST: /api/v2/books/order-service
    @PostMapping("/list/book-review")
    public List<BookReviewResponse> getBookReviewList(@RequestBody BookReviewRequest bookReviewRequest){
        return reviewService.findBookReviewList(bookReviewRequest.userId(), bookReviewRequest.bookOrderDetailRequests());
    }

    @PostMapping("/order-cancel")
    public ResponseEntity<Void> orderCancel(@RequestBody OrderCancelRequest orderCancelRequest){
        bookFacade.orderCancel(orderCancelRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
