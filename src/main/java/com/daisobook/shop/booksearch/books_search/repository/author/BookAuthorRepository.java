package com.daisobook.shop.booksearch.books_search.repository.author;

import com.daisobook.shop.booksearch.books_search.entity.author.BookAuthor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookAuthorRepository extends JpaRepository<BookAuthor, Long> {
//    boolean existsByAuthor_Id(long authorId);
//
//    boolean existsByRole_Id(long roleId);
//
//    void deleteAllByIdIn(List<Long> ids);
//
//    List<BookAuthor> findAllByRole_Id(long roleId);
}
