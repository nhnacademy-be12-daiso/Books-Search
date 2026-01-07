package com.daisobook.shop.booksearch.books_search.repository.book;

import com.daisobook.shop.booksearch.books_search.dto.projection.*;
import com.daisobook.shop.booksearch.books_search.entity.book.Book;
import com.daisobook.shop.booksearch.books_search.entity.book.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    boolean existsBookById(long id);

    boolean existsBookByIsbn(String isbn);

    Book findBookById(long id);

    List<Book> findAllByIdIn(List<Long> ids);

    @Query("SELECT b.isbn FROM Book b WHERE b.isbn IN :isbns")
    List<BookIsbnProjection> findBooksByIsbnIn(List<String> isbns);

    @Query("SELECT b.id AS id FROM Book b WHERE b.isbn = ?1")
    BookIdProjection getBookId(String isbn);

@Query(value = """
                SELECT DISTINCT
                    b.book_id as id,
                    b.isbn,
                    b.title,
                    b.indexs as `index`,
                    b.description,
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
                    b.stock,
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
                            JSON_OBJECT('categoryId', c.category_id, 'categoryName', c.category_name, 'deep', c.deep, 'preCategoryId', pc.category_id, 'preCategoryName', pc.category_name)
                        )
                        FROM book_categories bc
                        LEFT JOIN categories c ON bc.category_id = c.category_id
                        LEFT JOIN categories pc ON c.pre_category_id = pc.category_id
                        WHERE bc.book_id = b.book_id
                    ) AS categories,
                    (
                        SELECT JSON_ARRAYAGG(
                            JSON_OBJECT('tagId', t.tag_id, 'tagName', t.tag_name)
                        )
                        FROM book_tags bt
                        LEFT JOIN tags t ON bt.tag_id = t.tag_id
                        WHERE bt.book_id = b.book_id
                    ) AS tags,
                    b.volume_no as volumeNO,
                    b.is_packaging as isPackaging,
                    (
                        SELECT JSON_ARRAYAGG(
                            JSON_OBJECT('id', r.review_id, 'content', r.content, 'createdAt', DATE_FORMAT(r.created_at, '%Y-%m-%dT%H:%i:%s.%f+09:00'), 'modifiedAt', DATE_FORMAT(r.modified_at, '%Y-%m-%dT%H:%i:%s.%f+09:00'), 'rating', r.rating, 'userId', r.user_created_id,
                                'reviewImages',
                                (
                                    SELECT JSON_ARRAYAGG(
                                        JSON_OBJECT('no', ri.review_image_no, 'path', ri.review_image_path, 'imageType', ri.image_type)
                                    )
                                    FROM review_images ri
                                    WHERE ri.review_id = r.review_id
                                )
                            )
                        )
                        FROM reviews r
                        WHERE r.book_id = b.book_id
                    ) AS reviews,
                    b.is_deleted as isDeleted
                    FROM books b
                    LEFT JOIN publishers p ON b.publisher_id = p.publisher_id
                    WHERE b.book_id = :bookId
                    AND (
                            :includeDeleted = TRUE
                            OR b.is_deleted = 0
                        )
                """, nativeQuery = true)
BookDetailProjection getBookDetailById(@Param("bookId") Long bookId, @Param("includeDeleted") boolean includeDeleted);

    @Query(value = """
                SELECT DISTINCT
                    b.book_id as id,
                    b.isbn,
                    b.title,
                    b.indexs as 'index',
                    b.description,
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
                    b.stock,
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
                            JSON_OBJECT('categoryId', c.category_id, 'categoryName', c.category_name, 'deep', c.deep, 'preCategoryId', pc.category_id, 'preCategoryName', pc.category_name)
                        )
                        FROM book_categories bc
                        LEFT JOIN categories c ON bc.category_id = c.category_id
                        LEFT JOIN categories pc ON c.pre_category_id = pc.category_id
                        WHERE bc.book_id = b.book_id
                    ) AS categories,
                    (
                        SELECT JSON_ARRAYAGG(
                            JSON_OBJECT('tagId', t.tag_id, 'tagName', t.tag_name)
                        )
                        FROM book_tags bt
                        LEFT JOIN tags t ON bt.tag_id = t.tag_id
                        WHERE bt.book_id = b.book_id
                    ) AS tags,
                    b.volume_no as volumeNO,
                    b.is_packaging as isPackaging,
                    b.is_deleted as isDeleted
                    FROM books b
                    LEFT JOIN publishers p ON b.publisher_id = p.publisher_id
                    WHERE b.book_id = :bookId
                """, nativeQuery = true)
    BookUpdateViewProjection getBookUpdateView(@Param("bookId") Long bookId);

    @Query(value = """
            SELECT DISTINCT
                b.book_id as id,
                b.isbn,
                b.title,
                b.description,
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
                     JSON_OBJECT('categoryId', c.category_id, 'CategoryName', c.category_name, 'deep', c.deep, 'preCategoryId', pc.category_id, 'preCategoryName', pc.category_name)
                  )
                  FROM book_categories bc
                      LEFT JOIN categories c ON bc.category_id = c.category_id
                      LEFT JOIN categories pc ON c.pre_category_id = pc.category_id
                  WHERE bc.book_id = b.book_id
                ) AS categories,
                (
                  SELECT JSON_ARRAYAGG(
                     JSON_OBJECT('tagId', t.tag_id, 'tagName', t.tag_name)
                  )
                  FROM book_tags bt
                      LEFT JOIN tags t ON bt.tag_id = t.tag_id
                  WHERE bt.book_id = b.book_id
                ) AS tags,
                b.volume_no as volumeNO,
                b.is_packaging as isPackaging,
                b.is_deleted as isDeleted
                FROM books b
                LEFT JOIN publishers p ON b.publisher_id = p.publisher_id
                WHERE b.book_id IN (:bookIds)
                AND (
                        :includeDeleted = TRUE
                        OR b.is_deleted = 0
                      )
            """,
            nativeQuery = true
    )
    List<BookListProjection> getBookListBy(@Param("bookIds") List<Long> bookIds, @Param("includeDeleted") boolean includeDeleted);

    @Query(value = """
            SELECT DISTINCT
                b.book_id as bookId,
                b.title,
                b.price,
                b.stock,
                b.status,
                (
                  SELECT
                        bi.book_image_path
                  FROM book_images bi
                  WHERE bi.book_id = b.book_id AND bi.book_image_no = 1
                ) AS coverImage,
                b.volume_no as volumeNO,
                b.is_packaging as isPackaging
                FROM books b
                WHERE b.book_id IN (:bookIds)
                AND (
                        :includeDeleted = TRUE
                        OR b.is_deleted = 0
                      )
            """,
            nativeQuery = true
    )
    List<BookInfoListProjection> getBookInfoListBy(@Param("bookIds") List<Long> bookIds, @Param("includeDeleted") boolean includeDeleted);

    @Query(value = """
            SELECT b.book_id as id
            FROM books b
            WHERE b.publication_date > :startDate AND b.status != 'UNPUBLISHED'
            ORDER BY b.publication_date DESC
            """,
            nativeQuery = true
    )
    List<BookIdProjection> getBookIdByNewReleases(@Param("startDate")LocalDate startDate, Pageable pageable);

    @Query(value = """
            SELECT
                b.book_id as bookId,
                b.title,
                b.price
            FROM books b
            WHERE b.book_id IN (:bookIds)
            """,
            nativeQuery = true)
    List<BookSummeryProjection> getBookSummeryByIdIn(@Param("bookIds") List<Long> bookIds);

    @Query(value = """
            SELECT
                b.book_id as bookId,
                b.isbn,
                b.title,
                (
                  SELECT JSON_ARRAYAGG(
                     JSON_OBJECT('no', bi.book_image_no, 'path', bi.book_image_path, 'imageType', bi.image_type)
                  )
                  FROM book_images bi
                  WHERE bi.book_id = b.book_id
                ) AS images,
                b.price,
                b.stock,
                b.status,
                b.publication_date as publicationDate,
                p.publisher_name as publisher,
                b.is_deleted as isDeleted
            FROM books b
            JOIN publishers p ON b.publisher_id = p.publisher_id
            """,
            countQuery = "SELECT count(*) FROM books",
            nativeQuery = true)
    Page<BookAdminProjection> getBookAdminProjection(Pageable pageable);

    Long countAllByStatus(Status status);

    @Query(value = """
            SELECT
                b.book_id as id,
                b.isbn,
                b.title,
                b.description,
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
                     JSON_OBJECT('categoryId', c.category_id, 'CategoryName', c.category_name, 'deep', c.deep, 'preCategoryId', pc.category_id, 'preCategoryName', pc.category_name)
                  )
                  FROM book_categories bc
                      LEFT JOIN categories c ON bc.category_id = c.category_id
                      LEFT JOIN categories pc ON c.pre_category_id = pc.category_id
                  WHERE bc.book_id = b.book_id
                ) AS categories,
                (
                  SELECT JSON_ARRAYAGG(
                     JSON_OBJECT('tagId', t.tag_id, 'tagName', t.tag_name)
                  )
                  FROM book_tags bt
                      LEFT JOIN tags t ON bt.tag_id = t.tag_id
                  WHERE bt.book_id = b.book_id
                ) AS tags,
                b.volume_no as volumeNO,
                b.is_packaging as isPackaging,
                b.is_deleted as isDeleted
            FROM books b
            WHERE (
                        :includeDeleted = TRUE
                        OR b.is_deleted = 0
                    )
                    AND
                    EXISTS(
                            SELECT 1
                            FROM book_categories bc
                            WHERE bc.book_id = b.book_id
                                AND bc.category_id IN (:categoryIds)
                        )
            """,
            countQuery = """
                SELECT count(*) FROM books b
                WHERE (:includeDeleted = TRUE OR b.is_deleted = 0)
                AND EXISTS (
                SELECT 1 FROM book_categories bc
                WHERE bc.book_id = b.book_id AND bc.category_id IN (:categoryIds)
                )
            """,
            nativeQuery = true)
    Page<BookListProjection> getBookByCategoryIdIn(Pageable pageable, @Param("categoryIds") List<Long> categoryIds, @Param("includeDeleted") boolean includeDeleted);
}
