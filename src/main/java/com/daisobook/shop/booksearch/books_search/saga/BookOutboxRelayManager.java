package com.daisobook.shop.booksearch.books_search.saga;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class BookOutboxRelayManager {

    private final BookOutboxRelayProcessor bookOutboxRelayProcessor;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOutboxCommitted(BookOutboxCommittedEvent event) {
        bookOutboxRelayProcessor.processRelay(event.getOutboxId());
    }
}
