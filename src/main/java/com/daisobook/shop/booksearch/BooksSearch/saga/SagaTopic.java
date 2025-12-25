package com.daisobook.shop.booksearch.BooksSearch.saga;

import lombok.Getter;

@Getter
public class SagaTopic {

    // exchange는 하나로 돌려씀
    public static final String ORDER_EXCHANGE = "team3.saga.exchange";

    public static final String BOOK_QUEUE = "team3.saga.book.checkout";
    public static final String USER_QUEUE = "team3.saga.user.point-deduct";
    public static final String COUPON_QUEUE = "team3.saga.coupon.use";
    public static final String ORDER_QUEUE = "team3.saga.order.reply"; // 응답용 큐

    public static final String BOOK_RK = "command.book.checkout";
    public static final String USER_RK = "command.user.point-deduct";
    public static final String COUPON_RK = "command.coupon.use";

    public static final String REPLY_RK = "reply.order";


    // ==== 보상 관련 ====

    public static final String BOOK_COMPENSATION_RK = "compensate.book";
    public static final String USER_COMPENSATION_RK = "compensate.user";
    public static final String COUPON_COMPENSATION_RK = "compensate.coupon";
    public static final String REPLY_COMPENSATION_RK = "reply.compensate";

    public static final String BOOK_COMPENSATION_QUEUE = "team3.saga.book.rollback";
    public static final String USER_COMPENSATION_QUEUE = "team3.saga.user.rollback";
    public static final String COUPON_COMPENSATION_QUEUE = "team3.saga.coupon.rollback";
    public static final String ORDER_COMPENSATION_QUEUE = "team3.saga.rollback.reply";

}
