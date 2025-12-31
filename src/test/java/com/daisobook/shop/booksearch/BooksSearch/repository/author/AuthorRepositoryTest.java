package com.daisobook.shop.booksearch.BooksSearch.repository.author;

import com.daisobook.shop.booksearch.BooksSearch.entity.author.Author;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AuthorRepositoryTest {

    @Autowired
    private AuthorRepository authorRepository;

    @BeforeEach
    void setUp() {
        Author author1 = new Author("김영한");
        Author author2 = new Author("조영호");
        authorRepository.saveAll(List.of(author1, author2));
    }

    @Test
    @DisplayName("이름으로 작가 존재 여부를 확인한다")
    void existsByNameTest() {
        boolean exists = authorRepository.existsByName("김영한");
        boolean notExists = authorRepository.existsByName("없는작가");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("이름 목록에 포함된 모든 작가를 조회한다")
    void findAllByNameInTest() {
        List<String> names = List.of("김영한", "조영호", "신용권");
        
        List<Author> result = authorRepository.findAllByNameIn(names);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Author::getName)
                .containsExactlyInAnyOrder("김영한", "조영호");
    }
}