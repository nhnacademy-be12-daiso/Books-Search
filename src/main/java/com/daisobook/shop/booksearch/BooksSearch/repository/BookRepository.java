package com.daisobook.shop.booksearch.BooksSearch.repository;

import com.daisobook.shop.booksearch.BooksSearch.entity.Book;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    boolean existsBookById(long id);

    boolean existsBookByIsbn(String isbn);

    Book findBookById(long id);

    Book findBookByIsbn(String isbn);

//    List<Book> findAllByAuthor(String author);

    List<Book> findAllByIsbnIn(Collection<String> isbns);

    @Query("SELECT b FROM Book b " +
            "JOIN FETCH b.bookCategories bc " +
            "JOIN FETCH bc.category c " +
            "WHERE c.name = ?1")
    List<Book> findBooksByCategoryName(String categoryName);

    @Query("SELECT b FROM Book b " +
            "JOIN FETCH b.bookTags bt " +
            "JOIN FETCH bt.tag t " +
            "WHERE t.name = ?1")
    List<Book> findBooksByTagName(String tagName);

    List<Book> findAllByPublisher_Name(String publisherName);

    List<Book> findBooksByIdIn(List<Long> ids);

    List<Book> findAllByIdIn(List<Long> ids);

    List<Book> findAllByPublicationDateAfterOrderByPublicationDateDesc(LocalDate publicationDateAfter, Sort sort, Limit limit);
}
