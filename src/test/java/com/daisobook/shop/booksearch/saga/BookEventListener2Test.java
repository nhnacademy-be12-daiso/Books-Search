package com.daisobook.shop.booksearch.saga;

import com.daisobook.shop.booksearch.entity.saga.BookDeduplicationLog;
import com.daisobook.shop.booksearch.entity.saga.BookOutbox;
import com.daisobook.shop.booksearch.repository.saga.BookDeduplicationRepository;
import com.daisobook.shop.booksearch.repository.saga.BookOutboxRepository;
import com.daisobook.shop.booksearch.saga.event.OrderConfirmedEvent;
import com.daisobook.shop.booksearch.saga.todelete.BookEventListener2;
import com.daisobook.shop.booksearch.saga.todelete.CompensationOutboxService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookEventListener2Test {

    @InjectMocks
    private BookEventListener2 bookEventListener2;

    @Mock
    private BookDeduplicationRepository bookDeduplicationRepository;

    @Mock
    private BookOutboxRepository bookOutboxRepository;

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private CompensationOutboxService compensationOutboxService;

    @Spy // 실제 ObjectMapper 로직을 사용하기 위해 Spy 사용
    private ObjectMapper objectMapper = new ObjectMapper();

    private OrderConfirmedEvent createTestEvent(Long orderId) {
        return new OrderConfirmedEvent(
                UUID.randomUUID().toString(),
                orderId,
                1L, // userId
                1L, // outboxId
                Map.of(101L, 2), // bookList
                50000L, // totalAmount
                1000L,  // usedPoint
                500L,   // savedPoint
                List.of(1L) // usedCouponIds
        );
    }

    @Test
    @DisplayName("성공: 주문 확정 이벤트 수신 시 중복이 아니면 재고 로그와 Outbox를 저장한다")
    void handleOrderConfirmedEvent_Success_Test() {
        // given
        Long orderId = 100L;
        OrderConfirmedEvent event = createTestEvent(orderId);

        given(bookDeduplicationRepository.existsById(orderId)).willReturn(false);

        // when
        bookEventListener2.handleOrderConfirmedEvent(event);

        // then
        // 1. 중복 제거 로그 저장 확인
        verify(bookDeduplicationRepository, times(1)).save(any(BookDeduplicationLog.class));
        
        // 2. Outbox 저장 및 이벤트 발행 확인
        verify(bookOutboxRepository, times(1)).save(any(BookOutbox.class));
        verify(publisher, times(1)).publishEvent(any());
    }

    @Test
    @DisplayName("무시: 이미 처리된 주문 ID인 경우 로직을 수행하지 않고 종료한다")
    void handleOrderConfirmedEvent_Duplicate_Ignore_Test() {
        // given
        Long orderId = 100L;
        OrderConfirmedEvent event = createTestEvent(orderId);

        given(bookDeduplicationRepository.existsById(orderId)).willReturn(true);

        // when
        bookEventListener2.handleOrderConfirmedEvent(event);

        // then: 저장 로직들이 호출되지 않아야 함
        verify(bookDeduplicationRepository, never()).save(any());
        verify(bookOutboxRepository, never()).save(any());
        verify(publisher, never()).publishEvent(any());
    }
}