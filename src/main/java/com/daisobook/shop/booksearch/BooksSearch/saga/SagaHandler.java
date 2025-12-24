package com.daisobook.shop.booksearch.BooksSearch.saga;

import com.daisobook.shop.booksearch.BooksSearch.entity.saga.BookOutbox;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.saga.BookOutOfStockException;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.saga.FailedSerializationException;
import com.daisobook.shop.booksearch.BooksSearch.repository.saga.BookOutboxRepository;
import com.daisobook.shop.booksearch.BooksSearch.saga.event.OrderCompensateEvent;
import com.daisobook.shop.booksearch.BooksSearch.saga.event.OrderConfirmedEvent;
import com.daisobook.shop.booksearch.BooksSearch.saga.event.SagaEvent;
import com.daisobook.shop.booksearch.BooksSearch.saga.event.SagaReply;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class SagaHandler {

    private final ObjectMapper objectMapper;
    private final BookOutboxRepository outboxRepository;
    private final ApplicationEventPublisher publisher;
    private final SagaTestService testService;

    @Transactional
    public void handleEvent(OrderConfirmedEvent event) {

        boolean isSuccess = true; // 성공 여부
        String reason = null; // 실패시 사유

        try {
            // TODO 재고 차감 로직
            // 서비스 주입받아서 하시면 됨

//            testService.process(); // 일부러 재고 부족 터트리기

            // 로직 중에 재고 부족하면 해당 커스텀 예외 던지시면 됩니다.
            // 더 좋은 방법 있으면 추천 좀

            log.error("[Book API] 재고 차감 성공 - Order : {}", event.getOrderId());

        } catch(BookOutOfStockException e) { // 재고 부족 비즈니스 예외
            log.error("[Book API] 재고 부족으로 인한 차감 실패 - Order : {}", event.getOrderId());
            isSuccess = false;
            reason = "OUT_OF_STOCK";
        } catch(Exception e) {
            log.error("[Book API] 예상치 못한 시스템 에러 발생 - Order : {}", event.getOrderId(), e);
            isSuccess = false;
            reason = "SYSTEM_ERROR";
            // 이렇게 예외 범위를 넓게 해놔야 무슨 에러가 터져도 finally 문이 실행됨
        }
        finally {
            // 성공했든 실패했든 답장은 해야함
            SagaReply reply = new SagaReply(
                    event.getOrderId(),
                    "BOOK",
                    isSuccess,
                    reason
            );

            // 응답 메시지 전송
            this.send(event, reply, SagaTopic.REPLY_RK);
        }
    }

    @Transactional
    public void handleRollbackEvent(OrderCompensateEvent event) {

        boolean isSuccess = true; // 성공 여부
        String reason = null; // 실패시 사유

        try {
            // TODO 재고 '보상' 로직
            // 서비스 주입받아서 하시면 됨

            log.error("[Book API] 재고 보상 성공 - Order : {}", event.getOrderId());

        } catch(Exception e) {
            log.error("[Book API] 예상치 못한 시스템 에러 발생 - Order : {}", event.getOrderId(), e);
            isSuccess = false;
            reason = "SYSTEM_ERROR";
        }
        finally {
            // 성공했든 실패했든 답장은 해야함
            SagaReply reply = new SagaReply(
                    event.getOrderId(),
                    "BOOK",
                    isSuccess,
                    reason
            );

            // 응답 메시지 전송
            this.send(event, reply, SagaTopic.REPLY_COMPENSATION_RK);
        }
    }

    public void send(SagaEvent event, SagaReply reply, String key) {
        try {
            BookOutbox outbox = new BookOutbox(
                event.getOrderId(),
                "BOOK",
                SagaTopic.ORDER_EXCHANGE,
                key,
                objectMapper.writeValueAsString(reply)
            );

            outboxRepository.save(outbox);

            log.info("[Saga Outbox] {} 토픽으로 메시지 저장 완료 (OrderID: {})", SagaTopic.REPLY_RK, event.getOrderId());
            publisher.publishEvent(new BookOutboxCommittedEvent(this, outbox.getId()));


        } catch (JsonProcessingException e) {
            log.warn("객체 직렬화 실패");
            throw new FailedSerializationException("객체 직렬화 실패");
        }
    }
}

