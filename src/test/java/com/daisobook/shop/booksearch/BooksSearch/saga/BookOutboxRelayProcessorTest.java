package com.daisobook.shop.booksearch.BooksSearch.saga;

import com.daisobook.shop.booksearch.BooksSearch.entity.saga.BookOutbox;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.saga.ExternalServiceException;
import com.daisobook.shop.booksearch.BooksSearch.repository.saga.BookOutboxRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookOutboxRelayProcessorTest {

    @InjectMocks
    private BookOutboxRelayProcessor bookOutboxRelayProcessor;

    @Mock
    private BookEventPublisher bookEventPublisher;

    @Mock
    private BookOutboxRepository bookOutboxRepository;

    @Test
    @DisplayName("성공: 메시지 발행 성공 시 Outbox 상태를 PUBLISHED로 변경하고 저장한다")
    void processRelay_Success_Test() {
        // given
        Long outboxId = 1L;
        BookOutbox outbox = spy(new BookOutbox(100L, "BOOK", "topic", "key", "{}"));
        given(bookOutboxRepository.findById(outboxId)).willReturn(Optional.of(outbox));

        // when
        bookOutboxRelayProcessor.processRelay(outboxId);

        // then
        verify(bookEventPublisher).publishBookOutboxMessage(anyString(), anyString(), anyString());
        verify(outbox).markAsPublished(); // 상태 변경 메서드 호출 확인
        verify(bookOutboxRepository).save(outbox);
    }

    @Test
    @DisplayName("재시도: 발행 실패 시 재시도 횟수가 3회 미만이면 횟수를 증가시키고 예외를 던진다")
    void processRelay_Retry_Test() {
        // given
        Long outboxId = 1L;
        BookOutbox outbox = spy(new BookOutbox(100L, "BOOK", "topic", "key", "{}"));
        // 초기 재시도 횟수 0
        given(bookOutboxRepository.findById(outboxId)).willReturn(Optional.of(outbox));
        
        doThrow(new ExternalServiceException("RabbitMQ Fail"))
                .when(bookEventPublisher).publishBookOutboxMessage(anyString(), anyString(), anyString());

        // when & then
        assertThatThrownBy(() -> bookOutboxRelayProcessor.processRelay(outboxId))
                .isInstanceOf(ExternalServiceException.class);
        
        verify(outbox).incrementRetryCount(); // 횟수 증가 확인
        verify(bookOutboxRepository).save(outbox);
        verify(outbox, never()).markAsFailed(); // 아직 최종 실패는 아님
    }

    @Test
    @DisplayName("최종 실패: 재시도 횟수가 3회 이상이면 상태를 FAILED로 변경하고 예외를 던진다")
    void processRelay_Final_Fail_Test() {
        // given
        Long outboxId = 1L;
        BookOutbox outbox = spy(new BookOutbox(100L, "BOOK", "topic", "key", "{}"));
        
        // 억지로 재시도 횟수를 3으로 만듦 (비즈니스 로직에 따라 3회째 실패 시를 가정)
        for(int i=0; i<3; i++) outbox.incrementRetryCount();
        
        given(bookOutboxRepository.findById(outboxId)).willReturn(Optional.of(outbox));
        doThrow(new ExternalServiceException("RabbitMQ Fail"))
                .when(bookEventPublisher).publishBookOutboxMessage(anyString(), anyString(), anyString());

        // when & then
        assertThatThrownBy(() -> bookOutboxRelayProcessor.processRelay(outboxId))
                .isInstanceOf(ExternalServiceException.class);
        
        verify(outbox).markAsFailed(); // 최종 실패 처리 확인
        verify(bookOutboxRepository).save(outbox);
    }
}