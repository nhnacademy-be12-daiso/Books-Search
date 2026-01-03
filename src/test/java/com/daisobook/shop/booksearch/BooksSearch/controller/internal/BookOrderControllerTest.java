package com.daisobook.shop.booksearch.BooksSearch.controller.internal;

import com.daisobook.shop.booksearch.BooksSearch.controller.internal.order.BookOrderController;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.BookIdListReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.order.BookReviewRequest;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.order.OrderCancelRequest;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.order.OrderBooksInfoRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.service.book.impl.BookFacade;
import com.daisobook.shop.booksearch.BooksSearch.service.review.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookOrderController.class)
class BookOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookFacade bookFacade;

    @MockitoBean
    private ReviewService reviewService;

    @Test
    @DisplayName("주문 도서 정보 리스트 조회 API 테스트")
    void getOrderBookInfoList_Test() throws Exception {
        // Given
        BookIdListReqDTO req = new BookIdListReqDTO(List.of(1L, 2L));
        when(bookFacade.findBooksByIdIn(anyList())).thenReturn(new OrderBooksInfoRespDTO(List.of()));

        // When & Then
        mockMvc.perform(post("/api/v2/books/order-service/books/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("도서 요약 정보 리스트 조회 API 테스트")
    void getBookList_Test() throws Exception {
        // Given
        BookIdListReqDTO req = new BookIdListReqDTO(List.of(1L));
        when(bookFacade.getOrderBookList(anyList())).thenReturn(List.of());

        // When & Then
        mockMvc.perform(post("/api/v2/books/order-service/books/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("도서 리뷰 가능 목록 조회 API 테스트")
    void getBookReviewList_Test() throws Exception {
        // Given
        BookReviewRequest req = new BookReviewRequest(100L, List.of());
        when(reviewService.findBookReviewList(anyLong(), anyList())).thenReturn(List.of());

        // When & Then
        mockMvc.perform(post("/api/v2/books/order-service/list/book-review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("주문 취소 시 도서 재고 복구 호출 테스트")
    void orderCancel_Test() throws Exception {
        // Given
        OrderCancelRequest req = new OrderCancelRequest(1L, 3);
        doNothing().when(bookFacade).orderCancel(any(OrderCancelRequest.class));

        // When & Then
        mockMvc.perform(post("/api/v2/books/order-service/order-cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());
    }
}