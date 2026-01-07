package com.daisobook.shop.booksearch.books_search.repository;

import com.daisobook.shop.booksearch.books_search.dto.projection.BookIdProjection;
import com.daisobook.shop.booksearch.books_search.entity.BookOfTheWeek;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookOfTheWeekRepository extends JpaRepository<BookOfTheWeek, Long> {

    @Query(value = """
        SELECT b.book_id as id
        FROM book_of_the_week b
        WHERE b.is_active = true
        ORDER BY b.no DESC
    """,
    nativeQuery = true
    )
    List<BookIdProjection> getBookId(Pageable pageable);
}
