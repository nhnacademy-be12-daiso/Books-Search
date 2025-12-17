package com.daisobook.shop.booksearch.BooksSearch.saga;

import com.daisobook.shop.booksearch.BooksSearch.entity.saga.BookOutbox;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.saga.ExternalServiceException;
import com.daisobook.shop.booksearch.BooksSearch.repository.saga.BookOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookOutboxRelayProcessor {

    private final BookEventPublisher bookEventPublisher;
    private final BookOutboxRepository bookOutboxRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processRelay(Long outboxId) {

        BookOutbox outbox = bookOutboxRepository.findById(outboxId).orElseThrow();

        try {
            bookEventPublisher.publishBookOutboxMessage(
                    outbox.getTopic(),
                    outbox.getRoutingKey(),
                    outbox.getPayload()
            );
            outbox.markAsPublished();
            bookOutboxRepository.save(outbox);


        } catch (ExternalServiceException e) { // 실패시 재시도 및 롤백
            if (outbox.getRetryCount() < 3) {
                outbox.incrementRetryCount();
                bookOutboxRepository.save(outbox); // DB에 업데이트
            } else {
                outbox.markAsFailed();
                bookOutboxRepository.save(outbox); // DB에 업데이트
                log.error("[Book API] Outbox 메세지 최종 발행 실패 OutboxID : {}", outboxId);
            }
            throw e; // 예외 던져서 롤백 유도
        }
    }
}
