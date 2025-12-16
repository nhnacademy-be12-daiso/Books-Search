package com.daisobook.shop.booksearch.BooksSearch.repository.book;

import com.daisobook.shop.booksearch.BooksSearch.dto.projection.BookIsbnProjection;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
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

    @Query("SELECT b FROM Book b JOIN FETCH b.reviews WHERE b.id IN :bookIds")
    List<Book> findAllByIdWithReviews(List<Long> bookIds);

    @Query("SELECT b.isbn FROM Book b WHERE b.isbn IN :isbns")
    List<BookIsbnProjection> findBooksByIsbnIn(List<String> isbns);

    @Query("""
    SELECT DISTINCT b
    FROM Book b
    LEFT JOIN FETCH b.bookAuthors ba
    LEFT JOIN FETCH ba.author
    LEFT JOIN FETCH ba.role
    LEFT JOIN FETCH b.bookCategories bc
    LEFT JOIN FETCH bc.category
    LEFT JOIN FETCH b.bookTags bt
    LEFT JOIN FETCH bt.tag
    LEFT JOIN FETCH b.bookImages bi
    LEFT JOIN FETCH b.publisher p
    WHERE b.id = ?1
""")
    Book getBookById(long id);
}
