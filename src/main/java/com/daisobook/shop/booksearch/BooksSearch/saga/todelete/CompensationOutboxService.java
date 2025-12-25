package com.daisobook.shop.booksearch.BooksSearch.saga.todelete;


import com.daisobook.shop.booksearch.BooksSearch.entity.saga.BookDeduplicationLog;
import com.daisobook.shop.booksearch.BooksSearch.entity.saga.BookOutbox;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.saga.FailedSerializationException;
import com.daisobook.shop.booksearch.BooksSearch.repository.saga.BookDeduplicationRepository;
import com.daisobook.shop.booksearch.BooksSearch.repository.saga.BookOutboxRepository;
import com.daisobook.shop.booksearch.BooksSearch.saga.BookOutboxCommittedEvent;
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
public class CompensationOutboxService {
    private final BookOutboxRepository bookOutboxRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher publisher;
    private final BookDeduplicationRepository bookDeduplicationRepository;

    // 기존 트랜잭션과 상관 없는 새 트랜잭션을 시작함
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveCompensationEvent(Long orderId, Object event, SagaTopic2 topic) {

        String dedupKey = orderId + "_BOOK_FAIL";
        // 중복 검사
        if(bookDeduplicationRepository.existsByMessageId(dedupKey)) {
            log.warn("[Compensation] 중복 이벤트 수신 및 무시 : {}", orderId);
            return;
        }

        try {

            // 멱등성 보장을 위한 로그 기록
            BookDeduplicationLog logEntry = new BookDeduplicationLog(dedupKey);
            bookDeduplicationRepository.save(logEntry);

            BookOutbox outbox = new BookOutbox(
                    orderId,
                    "BOOK",
                    topic.getExchange(),
                    topic.getRoutingKey(),
                    objectMapper.writeValueAsString(event)
            );
            // 이벤트 발행
            bookOutboxRepository.save(outbox);
            publisher.publishEvent(new BookOutboxCommittedEvent(this, outbox.getId()));
            // ---> 얘가 알아서 쏴줌

        } catch (JsonProcessingException ex) {
            log.warn("객체 직렬화 실패");
            throw new FailedSerializationException("Failed to serialize event payload");
       }
    }



}

