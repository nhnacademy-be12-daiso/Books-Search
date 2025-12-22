package com.daisobook.shop.booksearch.BooksSearch.saga;

import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.entity.saga.BookDeduplicationLog;
import com.daisobook.shop.booksearch.BooksSearch.entity.saga.BookOutbox;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.saga.BookOutOfStockException;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.saga.FailedSerializationException;
import com.daisobook.shop.booksearch.BooksSearch.repository.saga.BookDeduplicationRepository;
import com.daisobook.shop.booksearch.BooksSearch.repository.saga.BookOutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookEventListener {

    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher publisher;
    private final BookDeduplicationRepository bookDeduplicationRepository;
    private final BookOutboxRepository bookOutboxRepository;

    @Value("${rabbitmq.routing.deducted}")
    private String routingKey;

    @RabbitListener(queues = "${rabbitmq.queue.book}")
    @Transactional
    public void handleOrderConfirmedEvent(OrderConfirmedEvent event) {
        log.info("[Book API] ===== 주문 확정 이벤트 수신됨 =====");
        log.info("[Book API] Order ID : {}", event.getOrderId());

        Long msgId = event.getOrderId();
        if(bookDeduplicationRepository.existsById(msgId)) {
            log.warn("[Book API] 중복 이벤트 수신 및 무시 : {}", msgId);
            return;
        }

        try {
            // TODO 실제 재고 차감 로직

            BookDeduplicationLog logEntry = new BookDeduplicationLog(msgId.toString());
            bookDeduplicationRepository.save(logEntry);
            // 멱등성을 위한 로그 기록

            try {
                BookOutbox outbox = new BookOutbox(
                        event.getOrderId(),
                        "BOOK",
                        "team3.saga.book.exchange",
                        routingKey,
                        objectMapper.writeValueAsString(event)
                );
                bookOutboxRepository.save(outbox);
                publisher.publishEvent(new BookOutboxCommittedEvent(this, outbox.getId()));
                // 커밋 이벤트 발행

            } catch (JsonProcessingException e) {
                log.warn("객체 직렬화 실패");
                throw new FailedSerializationException("Failed to serialize event payload");
            }


            log.info("[Book API] 재고 차감 성공");

        } catch(BookOutOfStockException e) { // <<<<<<<<<<<< 예외 처리 제대로 하기
            // TODO 재고 부족 혹은 실패 시 보상 트랜잭션 이벤트 발행
            log.error("[Book API] ===== 재고 부족으로 인한 보상 트랜잭션 시작 =====");
            log.error("[Book API] Order ID : {}", event.getOrderId());

            throw e;  // 트랜잭션 걸려있으므로 예외 던지면 DB 트랜잭션 롤백됨
        }
        catch(Exception e) {
            log.error("[Book API] 이벤트 처리 중 예상치 못한 오류 발생 : {}", e.getMessage());
            // DLQ 처리
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
            // ----> 이 예외를 날리면 Retry와 DLQ 플로우 시작함
        }
    }
}
