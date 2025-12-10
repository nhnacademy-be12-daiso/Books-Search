package com.daisobook.shop.booksearch.BooksSearch.repository.author;

import com.daisobook.shop.booksearch.BooksSearch.entity.author.Author;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AuthorRepository extends JpaRepository<Author, Long> {
    boolean existsByName(String name);

    Author findAuthorByName(String name);

    Author findAuthorById(long id);

    List<Author> findAllByNameIn(Collection<String> names);
}
