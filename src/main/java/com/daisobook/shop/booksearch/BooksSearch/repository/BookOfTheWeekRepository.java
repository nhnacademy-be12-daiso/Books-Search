package com.daisobook.shop.booksearch.BooksSearch.repository;

import com.daisobook.shop.booksearch.BooksSearch.dto.projection.BookIdProjection;
import com.daisobook.shop.booksearch.BooksSearch.entity.BookOfTheWeek;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookOfTheWeekRepository extends JpaRepository<BookOfTheWeek, Long> {
    List<BookOfTheWeek> findAllByIsActiveOrderByAppliedDateDesc(boolean isActive, Sort sort, Limit limit);

    @Query(value = """
        SELECT b.book_id
        FROM book_of_week b
        WHERE b.is_actice = true
        ORDER BY b.no DESC
    """,
    nativeQuery = true
    )
    List<BookIdProjection> getBookId(Pageable pageable);
}
