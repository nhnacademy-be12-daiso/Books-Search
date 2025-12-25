package com.daisobook.shop.booksearch.BooksSearch.saga.todelete;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SagaTopic2 {

    // 성공 트랜잭션
    // order -> book
    ORDER_SUCCESS("team3.saga.order.exchange", "team3.saga.book.queue.v2", "order.confirmed.v2"),
    // book -> user
    BOOK_SUCCESS("team3.saga.book.exchange", "team3.saga.user.queue", "inventory.deducted"),
    // user -> coupon
    USER_SUCCESS("team3.saga.user.exchange", "team3.saga.coupon.queue", "point.deducted"),
    // coupon -> payment
    COUPON_SUCCESS("team3.saga.coupon.exchange", "team3.saga.payment.queue", "coupon.used"),
    // payment -> order
    PAYMENT_SUCCESS("team3.saga.payment.exchange", "team3.saga.order.queue", "payment.success"),

    // 보상 트랜잭션 시
    // book -> order
    BOOK_COMPENSATION("team3.saga.book.exchange", "team3.saga.order.compensate.queue", "book.compensate"),
    // user -> book
    USER_COMPENSATION("team3.saga.user.exchange", "team3.saga.book.compensate.queue", "point.compensate"),
    // coupon -> user
    COUPON_COMPENSATION("team3.saga.coupon.exchange", "team3.saga.user.compensate.queue", "coupon.compensate"),
    // payment -> coupon
    PAYMENT_COMPENSATION("team3.saga.payment.exchange", "team3.saga.coupon.compensate.queue", "payment.compensate"),

    // OrderAPI로 우선 알림
    USER_NOTIFICATION("team3.saga.user.exchange", "team3.saga.order.notice.queue", "point.notice"),
    // coupon -> user
    COUPON_NOTIFICATION("team3.saga.coupon.exchange", "team3.saga.order.notice.queue", "coupon.notice"),
    // payment -> coupon
    PAYMENT_NOTIFICATION("team3.saga.payment.exchange", "team3.saga.order.notice.queue", "payment.notice");


    private final String exchange;
    private final String queue;
    private final String routingKey;

    private static boolean isDevMode = false;

    public static void setMode(boolean isDev) {
        isDevMode = isDev;
    }

    public String getQueue() {
        return isDevMode ?  queue + ".dev" : queue;
    }

    public String getRoutingKey() {
        return isDevMode ? routingKey + ".dev" : routingKey;
    }
}

