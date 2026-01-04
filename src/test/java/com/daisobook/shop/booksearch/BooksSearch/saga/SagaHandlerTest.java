package com.daisobook.shop.booksearch.BooksSearch.saga;

import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Status;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.saga.BookOutOfStockException;
import com.daisobook.shop.booksearch.BooksSearch.saga.event.OrderConfirmedEvent;
import com.daisobook.shop.booksearch.BooksSearch.saga.event.SagaReply;
import com.daisobook.shop.booksearch.BooksSearch.service.book.impl.BookCoreService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SagaHandlerTest {

    @InjectMocks
    private SagaHandler sagaHandler;

    @Mock
    private BookCoreService bookCoreService;

    @Mock
    private SagaReplyService replyService;

    // 테스트용 이벤트 생성 헬퍼
    private OrderConfirmedEvent createOrderEvent(Long orderId, Map<Long, Integer> bookList) {
        return new OrderConfirmedEvent(UUID.randomUUID().toString(), orderId, 1L, 1L, bookList, 10000L, 0L, 0L, List.of());
    }

    @Test
    @DisplayName("성공: 재고가 충분하면 차감 후 성공 응답을 보낸다")
    void handleOrderConfirmed_Success_Test() {
        // given
        Long bookId = 0L;
        OrderConfirmedEvent event = createOrderEvent(100L, Map.of(bookId, 2));
        Book book = new Book("9788998139766", "테스트 도서", "인덱스", "설명",
                LocalDate.now(), 20000L, true, 10, Status.ON_SALE, 1);
        
        given(bookCoreService.getBookByIdIn(anyList())).willReturn(List.of(book));

        // when
        sagaHandler.handleEvent(event);

        // then
        assertThat(book.getStock()).isEqualTo(8);
        verify(replyService).send(eq(event), argThat(SagaReply::isSuccess), any());
    }

    @Test
    @DisplayName("실패: 재고가 부족하면 예외를 던지고 실패 응답을 보낸다")
    void handleOrderConfirmed_OutOfStock_Test() {
        // given
        Long bookId = 0L;
        OrderConfirmedEvent event = createOrderEvent(100L, Map.of(bookId, 20)); // 재고보다 많은 주문
        Book book = new Book("9788998139766", "테스트 도서", "인덱스", "설명",
                LocalDate.now(), 20000L, true, 10, Status.ON_SALE, 1);
        
        given(bookCoreService.getBookByIdIn(anyList())).willReturn(List.of(book));

        // when & then
        assertThatThrownBy(() -> sagaHandler.handleEvent(event))
                .isInstanceOf(BookOutOfStockException.class);

        // finally 블록에 의해 실패 응답이 전송되었는지 확인
        verify(replyService).send(eq(event), argThat(reply -> !reply.isSuccess() && "OUT_OF_STOCK".equals(reply.getReason())), any());
    }

    @Test
    @DisplayName("실패: 도서가 존재하지 않으면 예외를 던지고 실패 응답을 보낸다")
    void handleOrderConfirmed_NotFound_Test() {
        // given
        OrderConfirmedEvent event = createOrderEvent(100L, Map.of(999L, 1));
        given(bookCoreService.getBookByIdIn(anyList())).willReturn(List.of()); // 빈 리스트 반환

        // when & then
        assertThatThrownBy(() -> sagaHandler.handleEvent(event))
                .isInstanceOf(BookOutOfStockException.class);

        verify(replyService).send(eq(event), argThat(reply -> "NOT_FOUND".equals(reply.getReason())), any());
    }
}