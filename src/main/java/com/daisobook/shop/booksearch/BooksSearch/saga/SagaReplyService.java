package com.daisobook.shop.booksearch.BooksSearch.saga;

import com.daisobook.shop.booksearch.BooksSearch.entity.saga.BookOutbox;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.saga.FailedSerializationException;
import com.daisobook.shop.booksearch.BooksSearch.repository.saga.BookOutboxRepository;
import com.daisobook.shop.booksearch.BooksSearch.saga.event.OrderConfirmedEvent;
import com.daisobook.shop.booksearch.BooksSearch.saga.event.SagaEvent;
import com.daisobook.shop.booksearch.BooksSearch.saga.event.SagaReply;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SagaReplyService {

    private final ObjectMapper objectMapper;
    private final BookOutboxRepository outboxRepository;
    private final ApplicationEventPublisher publisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void send(SagaEvent event, SagaReply reply, String key) {
        try {
            // 1. Outbox 엔티티 생성 및 저장
            BookOutbox outbox = new BookOutbox(
                    event.getOrderId(),
                    "BOOK",
                    SagaTopic.ORDER_EXCHANGE,
                    key,
                    objectMapper.writeValueAsString(reply)
            );

            outboxRepository.save(outbox);

            log.info("[Saga Reply] 독립 트랜잭션에 Outbox 저장 완료 (OrderID: {})", event.getOrderId());

            // 2. 리스너를 깨우기 위한 이벤트 발행
            // 이 이벤트는 이 '새로운 트랜잭션'이 성공적으로 커밋되는 순간 리스너를 호출함
            publisher.publishEvent(new BookOutboxCommittedEvent(this, outbox.getId()));

        } catch (JsonProcessingException e) {
            log.error("응답 메시지 직렬화 실패", e);
            throw new FailedSerializationException("응답 메시지 직렬화 실패");
        }
    }
}
