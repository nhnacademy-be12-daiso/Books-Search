package com.daisobook.shop.booksearch.saga;

import com.daisobook.shop.booksearch.entity.saga.BookOutbox;
import com.daisobook.shop.booksearch.exception.custom.saga.FailedSerializationException;
import com.daisobook.shop.booksearch.repository.saga.BookOutboxRepository;
import com.daisobook.shop.booksearch.saga.event.OrderConfirmedEvent;
import com.daisobook.shop.booksearch.saga.event.SagaReply;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SagaReplyServiceTest {

    @InjectMocks
    private SagaReplyService sagaReplyService;

    @Mock
    private BookOutboxRepository outboxRepository;

    @Mock
    private ApplicationEventPublisher publisher;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("성공: 응답 메시지를 JSON으로 변환하여 Outbox에 저장하고 이벤트를 발행한다")
    void send_Success_Test() {
        // given
        OrderConfirmedEvent event = new OrderConfirmedEvent(UUID.randomUUID().toString(), 1L, 1L, 1L, Map.of(1L, 1), 1000L, 0L, 0L, List.of());
        SagaReply reply = new SagaReply(event.getEventId(), event.getOrderId(), "BOOK", true, null);
        String key = "reply.key";

        // 💡 핵심: save 메서드가 호출되면 '자기 자신(전달받은 실제 객체)'을 그대로 반환하게 만듭니다.
        // 이렇게 하면 서비스 로직의 outbox 변수가 null이 되지 않고 흐름을 이어갑니다.
        given(outboxRepository.save(any(BookOutbox.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        sagaReplyService.send(event, reply, key);

        // then
        // 1. Repository에 저장이 시도되었는가?
        verify(outboxRepository).save(any(BookOutbox.class));

        // 2. OutboxCommittedEvent가 발행되었는가? (outbox.getId()가 내부적으로 호출됨을 의미)
        verify(publisher).publishEvent(any(BookOutboxCommittedEvent.class));
    }

    @Test
    @DisplayName("실패: 직렬화 에러 발생 시 FailedSerializationException을 던진다")
    void send_SerializationFail_Test() throws Exception {
        // given
        OrderConfirmedEvent event = new OrderConfirmedEvent(UUID.randomUUID().toString(), 1L, 1L, 1L, Map.of(1L, 1), 1000L, 0L, 0L, List.of());
        SagaReply reply = new SagaReply(event.getEventId(), event.getOrderId(), "BOOK", true, null);

        // ObjectMapper를 Mocking하여 강제로 예외 발생
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        SagaReplyService serviceWithMockMapper = new SagaReplyService(mockMapper, outboxRepository, publisher);
        
        given(mockMapper.writeValueAsString(any())).willThrow(JsonProcessingException.class);

        // when & then
        assertThatThrownBy(() -> serviceWithMockMapper.send(event, reply, "key"))
                .isInstanceOf(FailedSerializationException.class);
        
        verify(publisher, never()).publishEvent(any());
    }
}