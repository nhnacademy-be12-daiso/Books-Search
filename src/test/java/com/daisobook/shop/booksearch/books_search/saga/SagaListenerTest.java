package com.daisobook.shop.booksearch.books_search.saga;

import com.daisobook.shop.booksearch.books_search.entity.saga.BookDeduplicationLog;
import com.daisobook.shop.booksearch.books_search.repository.saga.BookDeduplicationRepository;
import com.daisobook.shop.booksearch.books_search.saga.event.OrderConfirmedEvent;
import com.daisobook.shop.booksearch.books_search.saga.event.SagaEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SagaListenerTest {

    @InjectMocks
    private SagaListener sagaListener;

    @Mock
    private BookDeduplicationRepository deduplicationRepository;

    @Mock
    private SagaHandler sagaHandler;

    // 테스트용 이벤트 생성 헬퍼 (중복 코드 방지)
    private SagaEvent createTestEvent() {
        return new OrderConfirmedEvent(
                UUID.randomUUID().toString(),
                100L, 1L, 1L, Map.of(1L, 1), 1000L, 0L, 0L, List.of()
        );
    }

    @Test
    @DisplayName("성공: 새로운 이벤트 수신 시 중복이 아니면 로그를 저장하고 핸들러를 호출한다")
    void onEvent_Success_Test() {
        // given
        SagaEvent event = createTestEvent();
        given(deduplicationRepository.existsByMessageId(anyString())).willReturn(false);

        // when
        sagaListener.onEvent(event);

        // then
        verify(deduplicationRepository).save(any(BookDeduplicationLog.class));
        verify(sagaHandler).onMessage(event);
    }

    @Test
    @DisplayName("무시: 이미 처리된 이벤트 ID인 경우 핸들러를 호출하지 않는다")
    void onEvent_Duplicate_Ignore_Test() {
        // given
        SagaEvent event = createTestEvent();
        given(deduplicationRepository.existsByMessageId(anyString())).willReturn(true);

        // when
        sagaListener.onEvent(event);

        // then
        verify(deduplicationRepository, never()).save(any());
        verify(sagaHandler, never()).onMessage(any());
    }

    @Test
    @DisplayName("성공: 보상 이벤트 수신 시 중복이 아니면 별도의 키로 로그를 저장하고 핸들러를 호출한다")
    void onCompensateEvent_Success_Test() {
        // given
        SagaEvent event = createTestEvent();
        String expectedDedupeKey = event.getEventId() + "_BOOK_COMP";
        given(deduplicationRepository.existsByMessageId(expectedDedupeKey)).willReturn(false);

        // when
        sagaListener.onCompensateEvent(event);

        // then
        verify(deduplicationRepository).save(argThat(log -> log.getMessageId().equals(expectedDedupeKey)));
        verify(sagaHandler).onMessage(event);
    }
}