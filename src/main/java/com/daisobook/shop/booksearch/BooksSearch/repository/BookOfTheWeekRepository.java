package com.daisobook.shop.booksearch.BooksSearch.repository;

import com.daisobook.shop.booksearch.BooksSearch.entity.BookOfTheWeek;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookOfTheWeekRepository extends JpaRepository<BookOfTheWeek, Long> {
    List<BookOfTheWeek> findAllByIsActiveOrderByAppliedDateDesc(boolean isActive, Sort sort, Limit limit);
}
