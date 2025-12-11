package com.daisobook.shop.booksearch.BooksSearch.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookEventListener {

    private static final String BOOK_QUEUE = "team3.order.confirmed.book.queue";

    private final BookEventPublisher bookEventPublisher; // 다음 이벤트 발행할 Publisher


    @RabbitListener(queues = BOOK_QUEUE)
    @Transactional
    public void handleOrderConfirmedEvent(OrderConfirmedEvent event) {
        log.info("[Book API] ===== 주문 확정 이벤트 수신됨 =====");
        log.info("[Book API] Order ID : {}", event.getOrderId());

        try {
            // TODO 실제 재고 차감 로직

            // 로컬 트랜잭션 성공
            // TODO saga의 다음 단계를 위한 이벤트 발행

            log.info("[Book API] 재고 차감 성공");
            log.info("[Book API] 다음 이벤트 발행 완료 : Book API -> User API");

        } catch(Exception e) { // 커스텀 예외 만들기!
            // TODO 재고 부족 혹은 실패 시 보상 트랜잭션 이벤트 발행
            log.error("[Book API] ===== 재고 부족으로 인한 보상 트랜잭션 시작 =====");
            log.error("[Book API] Order ID : {}", event.getOrderId());

            throw e;  // 트랜잭션 걸려있으므로 예외 던지면 DB 트랜잭션 롤백됨
        }
//        catch(Exception e) {
//            log.error("[Book API] 이벤트 처리 중 예상치 못한 오류 발생 : {}", e.getMessage());
//             TODO Dead Letter Queue 처리
//             ---> 근데 여기서도 보상 트랜잭션 날려야하는거 아님?
//        }

    }


}
