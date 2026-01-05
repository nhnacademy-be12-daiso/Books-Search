package com.daisobook.shop.booksearch.books_search.saga;

import com.daisobook.shop.booksearch.books_search.entity.book.Book;
import com.daisobook.shop.booksearch.books_search.entity.book.Status;
import com.daisobook.shop.booksearch.books_search.exception.custom.saga.BookOutOfStockException;
import com.daisobook.shop.booksearch.books_search.saga.event.OrderCompensateEvent;
import com.daisobook.shop.booksearch.books_search.saga.event.OrderConfirmedEvent;
import com.daisobook.shop.booksearch.books_search.saga.event.OrderRefundEvent;
import com.daisobook.shop.booksearch.books_search.saga.event.SagaReply;
import com.daisobook.shop.booksearch.books_search.service.book.impl.BookCoreService;
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

    // 헬퍼: 도서 생성
    private Book createBook( int stock, Status status) {
        // Book 엔티티에 ID 세팅이 필요할 경우를 위해 적절히 구성
        return new Book("9788998139766", "테스트 도서", "인덱스", "설명",
                LocalDate.now(), 20000L, true, stock, status, 1);
    }

    // 1. 반품(Refund) 성공 테스트
    @Test
    @DisplayName("반품 성공: 지정된 도서 ID의 재고를 수량만큼 증가시킨다")
    void handleOrderRefund_Success_Test() {
        // given
        Long bookId = 0L;
        Long refundQuantity = 3L;
        OrderRefundEvent refundEvent = new OrderRefundEvent(
                UUID.randomUUID().toString(), 100L, 1L, 1L, bookId, refundQuantity, 30000L
        );
        Book book = createBook(10, Status.ON_SALE);

        given(bookCoreService.getBook_Id(bookId)).willReturn(book);

        // when
        sagaHandler.handleEvent(refundEvent);

        // then
        assertThat(book.getStock()).isEqualTo(13); // 10 + 3
        verify(replyService).send(eq(refundEvent), argThat(SagaReply::isSuccess), any());
    }

    // 2. 보상(Compensate) - 주문 취소 보상 테스트
    @Test
    @DisplayName("보상 성공: 주문 확정 이벤트에 대한 보상 시 재고를 복구한다")
    void handleOrderCompensate_OrderConfirmed_Success() {
        // given: 2권을 주문했던 이벤트
        Long bookId = 0L;
        OrderConfirmedEvent originalOrder = new OrderConfirmedEvent(
                UUID.randomUUID().toString(), 100L, 1L, 1L, Map.of(bookId, 2), 20000L, 0L, 0L, List.of()
        );
        OrderCompensateEvent compensateEvent = new OrderCompensateEvent(
                UUID.randomUUID().toString(), originalOrder, "OTHER_SERVICE_FAILURE"
        );

        Book book = createBook(5, Status.ON_SALE);
        given(bookCoreService.getBookByIdIn(anyList())).willReturn(List.of(book));

        // when
        sagaHandler.handleEvent(compensateEvent);

        // then
        assertThat(book.getStock()).isEqualTo(7); // 5 + 2
        verify(replyService).send(eq(compensateEvent), argThat(SagaReply::isSuccess), any());
    }

    // 3. 주문 실패 케이스 - 도서 미존재 (Coverage 확보)
    @Test
    @DisplayName("주문 실패: 도서를 찾을 수 없는 경우 NOT_FOUND 응답")
    void handleOrderConfirmed_NotFound_Test() {
        // given
        OrderConfirmedEvent event = new OrderConfirmedEvent(
                UUID.randomUUID().toString(), 100L, 1L, 1L, Map.of(999L, 1), 10000L, 0L, 0L, List.of()
        );
        given(bookCoreService.getBookByIdIn(anyList())).willReturn(List.of()); // 검색 결과 없음

        // when & then
        assertThatThrownBy(() -> sagaHandler.handleEvent(event))
                .isInstanceOf(BookOutOfStockException.class);

        verify(replyService).send(eq(event), argThat(reply -> "NOT_FOUND".equals(reply.getReason())), any());
    }

    // 4. 예외 케이스 - 반품 시 도서 없음 (Catch 블록 커버리지)
    @Test
    @DisplayName("반품 실패: 반품하려는 도서 ID가 DB에 없으면 실패 응답")
    void handleOrderRefund_NotFound_Test() {
        // given
        OrderRefundEvent refundEvent = new OrderRefundEvent(
                UUID.randomUUID().toString(), 100L, 1L, 1L, 999L, 1L, 10000L
        );
        given(bookCoreService.getBook_Id(anyLong())).willReturn(null);

        // when & then
        assertThatThrownBy(() -> sagaHandler.handleEvent(refundEvent))
                .isInstanceOf(BookOutOfStockException.class);

        verify(replyService).send(eq(refundEvent), argThat(reply -> !reply.isSuccess()), any());
    }

    @Test
    @DisplayName("주문 성공: 재고가 딱 0이 되면 상태가 SOLD_OUT으로 변경되어야 함")
    void handleOrderConfirmed_BecomeSoldOut_Test() {
        Long bookId = 0L;
        OrderConfirmedEvent event = new OrderConfirmedEvent(UUID.randomUUID().toString(), 100L, 1L, 1L, Map.of(bookId, 10), 10000L, 0L, 0L, List.of());
        Book book = createBook(10, Status.ON_SALE); // 재고 10개

        given(bookCoreService.getBookByIdIn(anyList())).willReturn(List.of(book));

        sagaHandler.handleEvent(event);

        assertThat(book.getStock()).isZero();
        assertThat(book.getStatus()).isEqualTo(Status.SOLD_OUT); // 분기 커버리지: 재고 0 처리 로직
        verify(replyService).send(eq(event), argThat(SagaReply::isSuccess), any());
    }

    @Test
    @DisplayName("주문 실패: 수량이 0 이하인 비정상 요청")
    void handleOrderConfirmed_InvalidQuantity_Test() {
        Long bookId = 1L;
        OrderConfirmedEvent event = new OrderConfirmedEvent(UUID.randomUUID().toString(), 100L, 1L, 1L, Map.of(bookId, 0), 10000L, 0L, 0L, List.of());
        Book book = createBook(10, Status.ON_SALE);

        given(bookCoreService.getBookByIdIn(anyList())).willReturn(List.of(book));

        assertThatThrownBy(() -> sagaHandler.handleEvent(event)).isInstanceOf(BookOutOfStockException.class);
        verify(replyService).send(eq(event), argThat(r -> "STOCK_ERROR".equals(r.getReason())), any());
    }

    @Test
    @DisplayName("주문 실패: 예상치 못한 시스템 예외 발생 시나리오")
    void handleOrderConfirmed_SystemError_Test() {
        OrderConfirmedEvent event = new OrderConfirmedEvent(UUID.randomUUID().toString(), 100L, 1L, 1L, Map.of(1L, 1), 10000L, 0L, 0L, List.of());

        given(bookCoreService.getBookByIdIn(anyList())).willThrow(new RuntimeException("DB Connection Fail"));

        assertThatThrownBy(() -> sagaHandler.handleEvent(event)).isInstanceOf(RuntimeException.class);
        verify(replyService).send(eq(event), argThat(r -> "SYSTEM_ERROR".equals(r.getReason())), any());
    }

    // --- [2. OrderRefundEvent 테스트] ---

    @Test
    @DisplayName("반품 실패: 수량 오류 (0개 반품)")
    void handleOrderRefund_InvalidQty_Test() {
        OrderRefundEvent event = new OrderRefundEvent(UUID.randomUUID().toString(), 100L, 1L, 1L, 50L, 0L, 0L);

        assertThatThrownBy(() -> sagaHandler.handleEvent(event)).isInstanceOf(BookOutOfStockException.class);
    }

    // --- [3. OrderCompensateEvent 테스트] ---

    @Test
    @DisplayName("보상 성공: SOLD_OUT 상태였던 도서가 재고 복구로 ON_SALE이 되어야 함")
    void handleOrderCompensate_RestoreStatus_Test() {
        OrderConfirmedEvent original = new OrderConfirmedEvent(UUID.randomUUID().toString(), 100L, 1L, 1L, Map.of(0L, 5), 50000L, 0L, 0L, List.of());
        OrderCompensateEvent compensate = new OrderCompensateEvent(UUID.randomUUID().toString(), original, "FAIL");

        Book book = createBook(0, Status.SOLD_OUT); // 현재 품절 상태
        given(bookCoreService.getBookByIdIn(anyList())).willReturn(List.of(book));

        sagaHandler.handleEvent(compensate);

        assertThat(book.getStock()).isEqualTo(5);
        assertThat(book.getStatus()).isEqualTo(Status.ON_SALE); // SOLD_OUT -> ON_SALE 로직 커버
        verify(replyService).send(eq(compensate), any(), any());
    }

    @Test
    @DisplayName("보상 실패: 보상 로직 수행 중 에러 발생 (재시도 필요 시나리오)")
    void handleOrderCompensate_Error_Test() {
        OrderConfirmedEvent original = new OrderConfirmedEvent(UUID.randomUUID().toString(), 100L, 1L, 1L, Map.of(1L, 5), 50000L, 0L, 0L, List.of());
        OrderCompensateEvent compensate = new OrderCompensateEvent(UUID.randomUUID().toString(), original, "FAIL");

        given(bookCoreService.getBookByIdIn(anyList())).willThrow(new RuntimeException("Compensation Fail"));

        sagaHandler.handleEvent(compensate); // 보상 로직은 내부 catch에서 에러를 삼키고 reply를 보냄

        verify(replyService).send(eq(compensate), argThat(r -> !r.isSuccess()), any());
    }
}