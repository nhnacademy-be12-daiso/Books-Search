package com.daisobook.shop.booksearch.books_search.repository.author;

import com.daisobook.shop.booksearch.books_search.entity.author.BookAuthor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookAuthorRepository extends JpaRepository<BookAuthor, Long> {
}
