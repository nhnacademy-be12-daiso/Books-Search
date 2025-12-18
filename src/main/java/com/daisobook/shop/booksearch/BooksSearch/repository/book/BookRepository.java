package com.daisobook.shop.booksearch.BooksSearch.repository.book;

import com.daisobook.shop.booksearch.BooksSearch.dto.projection.BookIdProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.BookIsbnProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.BookListProjection;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    LEFT JOIN FETCH b.bookImages
    LEFT JOIN FETCH b.publisher
    WHERE b.id = ?1
""")
    Book getBookById(long id);

    @Query("SELECT b.id FROM Book b WHERE b.isbn = ?1")
    BookIdProjection getBookId(String isbn);

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
    LEFT JOIN FETCH b.bookImages
    LEFT JOIN FETCH b.publisher
    LEFT JOIN FETCH b.reviews r
    LEFT JOIN FETCH r.reviewImages
    WHERE b.id = ?1
""")
    Book getBookDetailById(long id);

    @Query(value = """
            SELECT DISTINCT
                b.book_id as id,
                b.isbn,
                b.title,
                (
                  SELECT JSON_ARRAYAGG(
                     JSON_OBJECT('authorId', a.author_id, 'authorName', a.author_name, 'roleId', r.role_id, 'roleName', r.role_name)
                  )
                  FROM book_authors ba
                      LEFT JOIN authors a ON ba.author_id = a.author_id
                      LEFT JOIN roles r ON ba.role_id = r.role_id
                  WHERE ba.book_id = b.book_id
                ) AS authors,
                (
                  SELECT JSON_OBJECT('id', p.publisher_id, 'name', p.publisher_name)
                  FROM publishers p
                  WHERE b.publisher_id = p.publisher_id
                ) AS publisher,
                b.publication_date as publicationDate,
                b.price,
                b.status,
                (
                  SELECT JSON_ARRAYAGG(
                     JSON_OBJECT('no', bi.book_image_no, 'path', bi.book_image_path, 'imageType', bi.image_type)
                  )
                  FROM book_images bi
                  WHERE bi.book_id = b.book_id
                ) AS images,
                (
                  SELECT JSON_ARRAYAGG(
                     JSON_OBJECT('id', c.category_id, 'name', c.category_name, 'deep', c.deep, 'preCategoryId', pc.category_id, 'preCategoryName', pc.category_name)
                  )
                  FROM book_categories bc
                      LEFT JOIN categories c ON bc.category_id = c.category_id
                      LEFT JOIN categories pc ON c.pre_category_id = pc.category_id
                  WHERE bc.book_id = b.book_id
                ) AS categories,
                (
                  SELECT JSON_ARRAYAGG(
                     JSON_OBJECT('id', t.tag_id, 'name', t.tag_name)
                  )
                  FROM book_tags bt
                      LEFT JOIN tags t ON bt.tag_id = t.tag_id
                  WHERE bt.book_id = b.book_id
                ) AS tags,
                b.volume_no as volumeNO,
                b.is_packaging as isPackaging
                FROM books b
                LEFT JOIN publishers p ON b.publisher_id = p.publisher_id
                WHERE b.book_id IN (:bookIds)
                AND (
                        :includeDeleted = TRUE
                        OR b.is_deleted = FALSE
                      )
            """,
            nativeQuery = true
    )
    List<BookListProjection> getBookListBy(@Param("bookIds") List<Long> bookIds, @Param("includeDeleted") boolean includeDeleted);

    @Query(value = """
            SELECT b.book_id as id
            FROM books b
            WHERE b.publication_date > :startDate
            ORDER BY b.publication_date DESC
            """,
            nativeQuery = true
    )
    List<BookIdProjection> getBookIdByNewReleases(@Param("startDate")LocalDate startDate, Pageable pageable);
}
