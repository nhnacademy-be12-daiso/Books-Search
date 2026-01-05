package com.daisobook.shop.booksearch.saga;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;


@Getter
public class BookOutboxCommittedEvent extends ApplicationEvent {
    private final Long outboxId;
    public BookOutboxCommittedEvent(Object source, Long outboxId) {
        super(source);
        this.outboxId = outboxId;
    }
}
