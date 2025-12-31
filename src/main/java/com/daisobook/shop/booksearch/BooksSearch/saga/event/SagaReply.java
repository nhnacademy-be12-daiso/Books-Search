package com.daisobook.shop.booksearch.BooksSearch.saga.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 각 서비스가 각자의 로직을 마무리하고 Order에게 넘겨주는 공통 양식
 */

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SagaReply {

    private String eventId;
    private Long orderId;
    private String serviceName; // 어떤 서비스에서 왔는지?
    private boolean success; // 성공했는지
    private String reason;   // 실패 사유
}