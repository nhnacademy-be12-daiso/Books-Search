package com.daisobook.shop.booksearch.BooksSearch.saga;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.saga.ExternalServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookEventPublisherTest {

    @InjectMocks
    private BookEventPublisher bookEventPublisher;

    @Mock
    private AmqpTemplate rabbitTemplate;

    @Test
    @DisplayName("성공: 토픽, 라우팅 키, 페이로드가 주어지면 RabbitMQ로 메시지를 전송한다")
    void publishBookOutboxMessage_Success_Test() {
        // given
        String topic = "test.topic";
        String routingKey = "test.key";
        String payload = "{\"id\": 1}";

        // when
        bookEventPublisher.publishBookOutboxMessage(topic, routingKey, payload);

        // then
        // rabbitTemplate.send가 정확한 토픽과 라우팅 키로 한 번 호출되었는지 확인
        verify(rabbitTemplate, times(1)).send(eq(topic), eq(routingKey), any(Message.class));
    }

    @Test
    @DisplayName("실패: 전송 중 예외 발생 시 ExternalServiceException을 던진다")
    void publishBookOutboxMessage_Fail_Test() {
        // given
        String topic = "test.topic";
        String routingKey = "test.key";
        String payload = "{\"id\": 1}";

        // rabbitTemplate.send 호출 시 런타임 예외 강제 발생
        doThrow(new RuntimeException("RabbitMQ Connection Fail"))
                .when(rabbitTemplate).send(anyString(), anyString(), any(Message.class));

        // when & then
        // 소나큐브 권장: 람다 내 단일 호출 유지
        assertThatThrownBy(() -> bookEventPublisher.publishBookOutboxMessage(topic, routingKey, payload))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessageContaining("rabbitMQ 메세지 발행 실패");
    }
}