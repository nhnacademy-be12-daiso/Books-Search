package com.daisobook.shop.booksearch.books_search.search.component;


import com.daisobook.shop.booksearch.books_search.entity.book.Book;
import com.daisobook.shop.booksearch.books_search.search.domain.RabbitBook;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@RequiredArgsConstructor
public class BookSearchSyncPublisher {

    private final BookSearchTaskPublisher taskPublisher;
    private final BookSearchPayloadMapper payloadMapper;

    public void publishUpsertAfterCommit(Book book, String reason) {
        RabbitBook payload = payloadMapper.toRabbitBook(book);
        afterCommit(() -> taskPublisher.publishBookUpsert(payload, reason));
    }

    public void publishDeleteAfterCommit(String isbn, String reason) {
        afterCommit(() -> taskPublisher.publishBookDelete(isbn, reason));
    }

    private void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }
}
