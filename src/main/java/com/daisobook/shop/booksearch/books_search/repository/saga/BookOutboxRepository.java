package com.daisobook.shop.booksearch.books_search.repository.saga;

import com.daisobook.shop.booksearch.books_search.entity.saga.BookOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookOutboxRepository extends JpaRepository<BookOutbox, Long> {
}
