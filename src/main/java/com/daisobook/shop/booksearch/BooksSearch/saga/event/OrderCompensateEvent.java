package com.daisobook.shop.booksearch.BooksSearch.saga.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCompensateEvent implements SagaEvent {

    private Long orderId;
    private Long userId;
    private Long outboxId;

    private Map<Long, Integer> bookList;
    private Long totalAmount;
    private Long usedPoint; // 사용 포인트
    private Long savedPoint; // 적립 포인트
    private List<Long> usedCouponIds;

    private String failureReason; // 보상 트랜잭션 이유

    public OrderCompensateEvent(OrderConfirmedEvent event, String failureReason) {
        this.orderId = event.getOrderId();
        this.userId = event.getUserId();
//        this.outboxId = event.getOutboxId();  // ---> 얘는 다시 부여받지 않나..?
        this.bookList = event.getBookList();
        this.totalAmount = event.getTotalAmount();
        this.usedPoint = event.getUsedPoint();
        this.savedPoint = event.getSavedPoint();
        this.usedCouponIds = event.getUsedCouponIds();
        this.failureReason = failureReason;
    }
}
