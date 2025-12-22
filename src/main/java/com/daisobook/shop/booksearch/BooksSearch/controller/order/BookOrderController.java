package com.daisobook.shop.booksearch.BooksSearch.controller.order;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.BookIdListReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.order.OrderBooksInfoRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.service.book.impl.BookFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/order-service")
public class BookOrderController {
    private final BookFacade bookFacade;

    //POST: /api/v2/books/order-service/books/info
    @PostMapping("/books/info")
    public OrderBooksInfoRespDTO getOrderBookInfoList(@RequestBody BookIdListReqDTO bookIdListReqDTO){
        return bookFacade.findBooksByIdIn(bookIdListReqDTO.bookIdList());
    }
}
