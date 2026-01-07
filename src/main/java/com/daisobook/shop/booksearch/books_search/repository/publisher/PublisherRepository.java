package com.daisobook.shop.booksearch.books_search.repository.publisher;

import com.daisobook.shop.booksearch.books_search.entity.publisher.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PublisherRepository extends JpaRepository<Publisher, Long> {

    Publisher findPublisherByName(String name);

    List<Publisher> findAllByNameIn(Collection<String> names);
}
