package com.daisobook.shop.booksearch.BooksSearch.repository;

import com.daisobook.shop.booksearch.BooksSearch.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    boolean existsBookById(long id);

    boolean existsBookByIsbn(String isbn);

    List<Book> findAllById(long id);

    Book findBookById(long id);

    Book findBookByIsbn(String isbn);

    List<Book> findAllByAuthor(String author);

    List<Book> findAllByPublisher(String publisher);

    List<Book> findAllByIsbnIn(Collection<String> isbns);
}
