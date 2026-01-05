package com.daisobook.shop.booksearch.books_search.saga;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookOutboxRelayManagerTest {

    @InjectMocks
    private BookOutboxRelayManager bookOutboxRelayManager;

    @Mock
    private BookOutboxRelayProcessor bookOutboxRelayProcessor;

    @Test
    @DisplayName("성공: 커밋 이벤트 수신 시 해당 OutboxId로 프로세서를 호출해야 한다")
    void handleOutboxCommitted_Success_Test() {
        // given
        Long outboxId = 777L;
        BookOutboxCommittedEvent event = new BookOutboxCommittedEvent(this, outboxId);

        // when
        bookOutboxRelayManager.handleOutboxCommitted(event);

        // then
        // Processor의 processRelay 메서드가 정확한 ID와 함께 호출되었는지 검증
        verify(bookOutboxRelayProcessor, times(1)).processRelay(outboxId);
    }
}