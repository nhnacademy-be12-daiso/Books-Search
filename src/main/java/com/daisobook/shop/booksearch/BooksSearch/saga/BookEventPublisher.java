package com.daisobook.shop.booksearch.BooksSearch.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookEventPublisher {

    private final AmqpTemplate rabbitTemplate;

    private final String BOOK_EXCHANGE = "team3.saga.book.exchange";
    private final String ROUTING_KEY_DEDUCTED = "inventory.deducted";

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishBookDeductedEvent(OrderConfirmedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    BOOK_EXCHANGE,
                    ROUTING_KEY_DEDUCTED,
                    event
            );
            log.info("[Book API] 재고 차감 성공 이벤트 발행 완료 : {}", ROUTING_KEY_DEDUCTED);
        } catch (Exception e) {
            log.warn("[Book API] RabbitMQ 발행 실패 : {}", e.getMessage());
            // TODO : Outbox 패턴 또는 재시도 로직 구현해야함!!!
        }
    }
}
