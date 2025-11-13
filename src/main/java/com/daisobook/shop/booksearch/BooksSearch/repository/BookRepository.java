package com.daisobook.shop.booksearch.BooksSearch.repository;

import com.daisobook.shop.booksearch.BooksSearch.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}
