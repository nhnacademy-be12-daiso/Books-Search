package com.daisobook.shop.booksearch.repository.saga;

import com.daisobook.shop.booksearch.entity.saga.BookOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookOutboxRepository extends JpaRepository<BookOutbox, Long> {
}
