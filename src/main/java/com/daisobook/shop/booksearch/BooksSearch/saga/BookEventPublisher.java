package com.daisobook.shop.booksearch.BooksSearch.saga;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.saga.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookEventPublisher {

    private final AmqpTemplate rabbitTemplate;

    private final String BOOK_EXCHANGE = "team3.saga.book.exchange";
    @Value("${rabbitmq.routing.deducted}")
    private String ROUTING_KEY_DEDUCTED;

//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    public void publishBookDeductedEvent(OrderConfirmedEvent event) {
//        try {
//            rabbitTemplate.convertAndSend(
//                    BOOK_EXCHANGE,
//                    ROUTING_KEY_DEDUCTED,
//                    event
//            );
//            log.info("[Book API] 재고 차감 성공 이벤트 발행 완료 : {}", ROUTING_KEY_DEDUCTED);
//        } catch (Exception e) {
//            log.warn("[Book API] RabbitMQ 발행 실패 : {}", e.getMessage());
//            // TODO : Outbox 패턴 또는 재시도 로직 구현해야함!!!
//        }
//    }

    public void publishBookOutboxMessage(String topic, String routingKey, String payload) {

        try {
            byte[] body = payload.getBytes(StandardCharsets.UTF_8);

            MessageProperties properties = new MessageProperties();
            properties.setContentType(MessageProperties.CONTENT_TYPE_JSON); // 👈 핵심 수정
            properties.setContentEncoding("UTF-8");
            Message message = new Message(body);

            rabbitTemplate.send(topic, routingKey, message); // 직렬화 해서 생으로 보냄

            log.info("[Book API] 다음 이벤트 발행 완료 : Book API -> User API");

        } catch(Exception e) {
            log.warn("[Book API] RabbitMQ 발행 실패 : {}", e.getMessage());
            throw new ExternalServiceException("rabbitMQ 메세지 발행 실패");
        }
    }
}
