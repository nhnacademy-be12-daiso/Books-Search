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

    private final SagaTestService testService;
    private final SagaReplyService replyService;

    @Transactional
    public void handleEvent(OrderConfirmedEvent event) {

        boolean isSuccess = true; // 성공 여부
        String reason = null; // 실패시 사유

        try {
//            testService.process(); // 재고 부족 시나리오 테스트 용 --> 무시하셔도 됩니다.

            // TODO 01 재고 차감 로직 (김동건 님)
            /**
             *  본인들 서비스 주입받아서 로직 구현하시면 됩니다.
             *  매개변수로 넘어온 event DTO를 까보시면 필요한 정보들이 담겨 있습니다.
             *  그거 토대로 각자 로직에 구현해주면 됨 (재고 차감, 포인트 차감, 쿠폰 사용 처리)
             *
             *  만약 재고가 부족하다? 그럼 하단에 BookOutOfStockException을 던지면 됩니다!
             *
             *  더 좋은 로직 있다면 추천 가능
             */

            log.error("[Book API] 재고 차감 성공 - Order : {}", event.getOrderId());

        } catch(BookOutOfStockException e) { // 재고 부족 비즈니스 예외
            log.error("[Book API] 재고 부족으로 인한 차감 실패 - Order : {}", event.getOrderId());
            isSuccess = false;
            reason = "OUT_OF_STOCK";
            throw e; // 롤백
        } catch(Exception e) {
            log.error("[Book API] 예상치 못한 시스템 에러 발생 - Order : {}", event.getOrderId(), e);
            isSuccess = false;
            reason = "SYSTEM_ERROR";
            throw e; // 롤백
        }
        // 이렇게 예외 범위를 넓게 해놔야 무슨 에러가 터져도 finally 문이 실행됨
        finally {
            // 성공했든 실패했든 답장은 해야함
            SagaReply reply = new SagaReply(
                    event.getOrderId(),
                    "BOOK",
                    isSuccess,
                    reason
            );

            // 응답 메시지 전송
            replyService.send(event, reply, SagaTopic.REPLY_RK);
        }
    }

    @Transactional
    public void handleRollbackEvent(OrderCompensateEvent event) {

        boolean isSuccess = true; // 성공 여부
        String reason = null; // 실패시 사유

        try {
            // TODO 02 재고 '보상' 로직 (김동건 님)
            /**
             * 동일하게 서비스 주입받아서 하시면 되는데,
             * 여기서는 '뭔가 잘못돼서 다시 원복시키는 롤백'의 과정입니다.
             * 그니까 아까 차감했던 재고를 다시 원복시키는 로직을 구현하시면 됩니다.
             */

            log.error("[Book API] 재고 보상 성공 - Order : {}", event.getOrderId());

        } catch(Exception e) {
            log.error("[Book API] 예상치 못한 시스템 에러 발생 - Order : {}", event.getOrderId(), e);
            isSuccess = false;
            reason = "SYSTEM_ERROR";
            // TODO 재시도 로직
            /**
             * 보상 자체가 실패했을때 재시도 로직을 구현할 필요가 있음
             * 비즈니스 로직의 문제가 아니라 기술적 문제라면 재시도하는게 맞음
             * 그냥 무지성으로 롤백해버리면 안됨
             */
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
            replyService.send(event, reply, SagaTopic.REPLY_COMPENSATION_RK);
        }
    }
}

