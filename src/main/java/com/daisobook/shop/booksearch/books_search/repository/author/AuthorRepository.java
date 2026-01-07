package com.daisobook.shop.booksearch.books_search.repository.author;

import com.daisobook.shop.booksearch.books_search.entity.author.Author;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AuthorRepository extends JpaRepository<Author, Long> {
    boolean existsByName(String name);

    List<Author> findAllByNameIn(Collection<String> names);
}
