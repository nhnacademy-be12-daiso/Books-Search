package com.daisobook.shop.booksearch.BooksSearch.saga;

import com.daisobook.shop.booksearch.BooksSearch.entity.saga.BookDeduplicationLog;
import com.daisobook.shop.booksearch.BooksSearch.entity.saga.BookOutbox;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.saga.FailedSerializationException;
import com.daisobook.shop.booksearch.BooksSearch.repository.saga.BookDeduplicationRepository;
import com.daisobook.shop.booksearch.BooksSearch.repository.saga.BookOutboxRepository;
import com.daisobook.shop.booksearch.BooksSearch.saga.todelete.CompensationOutboxService;
import com.daisobook.shop.booksearch.BooksSearch.saga.todelete.SagaTopic2;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompensationOutboxServiceTest {

    @InjectMocks
    private CompensationOutboxService compensationOutboxService;

    @Mock
    private BookOutboxRepository bookOutboxRepository;

    @Mock
    private BookDeduplicationRepository bookDeduplicationRepository;

    @Mock
    private ApplicationEventPublisher publisher;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("성공: 보상 이벤트가 중복이 아니면 로그와 Outbox를 저장하고 이벤트를 발행한다")
    void saveCompensationEvent_Success_Test() throws Exception {
        // given
        Long orderId = 123L;
        Map<String, String> event = Map.of("reason", "test");
        SagaTopic2 topic = SagaTopic2.BOOK_COMPENSATION;
        String dedupKey = orderId + "_BOOK_FAIL";

        // 중복이 아님을 확실히 명시
        given(bookDeduplicationRepository.existsByMessageId(dedupKey)).willReturn(false);

        BookOutbox mockOutbox = mock(BookOutbox.class);
        lenient().when(mockOutbox.getId()).thenReturn(1L);
        lenient().when(bookOutboxRepository.save(any(BookOutbox.class))).thenReturn(mockOutbox);

        // when
        compensationOutboxService.saveCompensationEvent(orderId, event, topic);

        // then
        verify(bookDeduplicationRepository).save(any(BookDeduplicationLog.class));
        verify(bookOutboxRepository).save(any(BookOutbox.class));
    }

    @Test
    @DisplayName("무시: 이미 처리된 보상 이벤트인 경우 로직을 수행하지 않는다")
    void saveCompensationEvent_Duplicate_Ignore_Test() {
        // given
        Long orderId = 123L;
        given(bookDeduplicationRepository.existsByMessageId(anyString())).willReturn(true);

        // when
        compensationOutboxService.saveCompensationEvent(orderId, new Object(), SagaTopic2.BOOK_COMPENSATION);

        // then
        verify(bookDeduplicationRepository, never()).save(any());
        verify(bookOutboxRepository, never()).save(any());
        verify(publisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("실패: 객체 직렬화 실패 시 FailedSerializationException이 발생해야 한다")
    void saveCompensationEvent_Serialization_Fail_Test() throws Exception {
        // given
        Long orderId = 123L;
        Object event = new Object();
        given(bookDeduplicationRepository.existsByMessageId(anyString())).willReturn(false);
        
        // ObjectMapper가 예외를 던지도록 설정
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        CompensationOutboxService serviceWithMockMapper = new CompensationOutboxService(
                bookOutboxRepository, mockMapper, publisher, bookDeduplicationRepository);
        
        given(mockMapper.writeValueAsString(any())).willThrow(JsonProcessingException.class);

        // when & then (소나큐브 권장: 람다 내 단일 호출)
        assertThatThrownBy(() -> serviceWithMockMapper.saveCompensationEvent(orderId, event, SagaTopic2.BOOK_COMPENSATION))
                .isInstanceOf(FailedSerializationException.class)
                .hasMessageContaining("Failed to serialize");
    }
}