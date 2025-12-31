package com.daisobook.shop.booksearch.BooksSearch.saga.event;

import com.daisobook.shop.booksearch.BooksSearch.saga.SagaHandler;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCompensateEvent implements SagaEvent {

    @JsonProperty("eventId")
    private String eventId;
    private SagaEvent originalEvent; // ----> 이렇게 해야 어떤 이벤트든 담을 수 있음
    private String failureReason; // 실패 사유

    @Override
    public Long getOrderId() {
        return originalEvent.getOrderId();
    }

    @Override
    public void accept(SagaHandler handler) {
        handler.handleEvent(this);
    }
}
