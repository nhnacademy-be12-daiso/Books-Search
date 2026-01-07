package com.daisobook.shop.booksearch.books_search.repository.tag;

import com.daisobook.shop.booksearch.books_search.entity.tag.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findAllByNameIn(Collection<String> names);
}
