package com.daisobook.shop.booksearch.BooksSearch.saga;

import com.daisobook.shop.booksearch.BooksSearch.entity.saga.BookDeduplicationLog;
import com.daisobook.shop.booksearch.BooksSearch.repository.saga.BookDeduplicationRepository;
import com.daisobook.shop.booksearch.BooksSearch.saga.event.OrderCompensateEvent;
import com.daisobook.shop.booksearch.BooksSearch.saga.event.OrderConfirmedEvent;
import com.daisobook.shop.booksearch.BooksSearch.saga.event.SagaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class SagaListener {

    private final BookDeduplicationRepository deduplicationRepository;
    private final SagaHandler sagaHandler;

    @RabbitListener(queues = SagaTopic.BOOK_QUEUE)
    public void onEvent(SagaEvent event) {

        // 중복 검사
        if(deduplicationRepository.existsByMessageId(String.valueOf(event.getOrderId()))) {
            log.info("[Saga] 중복된 요청 무시 - Order ID : {} ", event.getOrderId());
            return;
        }

        log.info("[Saga] 주문 이벤트 수신 - OrderID: {}", event.getOrderId());

        // 멱등성 보장
        deduplicationRepository.save(new BookDeduplicationLog(event.getOrderId().toString()));

        // 실제 작업은 핸들러가
        sagaHandler.onMessage(event);
    }

    // 보상 로직
    @RabbitListener(queues = SagaTopic.BOOK_COMPENSATION_QUEUE)
    public void onCompensateEvent(SagaEvent event) {

        String dedupeKey = event.getOrderId() + "_BOOK_COMP";

        // 중복 검사
        if(deduplicationRepository.existsByMessageId(dedupeKey)) {
            log.info("[Saga] 중복된 보상 요청 무시 - Order ID : {} ", event.getOrderId());
            return;
        }

        log.info("[Saga] 보상 이벤트 수신 - OrderID: {}", event.getOrderId());

        // 멱등성 보장
        deduplicationRepository.save(new BookDeduplicationLog(dedupeKey));

        // 실제 작업은 핸들러가
        sagaHandler.onMessage(event);
    }
}
