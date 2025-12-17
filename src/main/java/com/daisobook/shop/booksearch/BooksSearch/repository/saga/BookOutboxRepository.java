package com.daisobook.shop.booksearch.BooksSearch.repository.saga;

import com.daisobook.shop.booksearch.BooksSearch.entity.saga.BookOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookOutboxRepository extends JpaRepository<BookOutbox, Long> {
}
