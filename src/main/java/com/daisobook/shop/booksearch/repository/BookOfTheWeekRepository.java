package com.daisobook.shop.booksearch.repository;

import com.daisobook.shop.booksearch.dto.projection.BookIdProjection;
import com.daisobook.shop.booksearch.entity.BookOfTheWeek;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookOfTheWeekRepository extends JpaRepository<BookOfTheWeek, Long> {
//    List<BookOfTheWeek> findAllByIsActiveOrderByAppliedDateDesc(boolean isActive, Sort sort, Limit limit);

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
