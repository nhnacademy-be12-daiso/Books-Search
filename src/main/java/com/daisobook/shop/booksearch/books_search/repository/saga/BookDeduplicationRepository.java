package com.daisobook.shop.booksearch.books_search.repository.saga;

import com.daisobook.shop.booksearch.books_search.entity.saga.BookDeduplicationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookDeduplicationRepository extends JpaRepository<BookDeduplicationLog, Long> {
    boolean existsByMessageId(String messageId);
}
