package com.daisobook.shop.booksearch.BooksSearch.repository.tag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import com.daisobook.shop.booksearch.BooksSearch.entity.tag.Tag;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TagRepositoryTest {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        // 테스트용 태그 데이터 준비
        entityManager.persist(new Tag("Spring"));
        entityManager.persist(new Tag("Java"));
        entityManager.persist(new Tag("Hibernate"));
        
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("여러 태그 이름 목록으로 존재하는 태그들을 모두 조회한다")
    void findAllByNameInTest() {
        // Given
        Collection<String> searchNames = List.of("Spring", "Java", "Python");

        // When
        List<Tag> results = tagRepository.findAllByNameIn(searchNames);

        // Then
        // Python은 없으므로 Spring, Java 총 2개가 조회되어야 함
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Tag::getName)
                           .containsExactlyInAnyOrder("Spring", "Java")
                           .doesNotContain("Python");
    }

    @Test
    @DisplayName("일치하는 이름이 하나도 없으면 빈 리스트를 반환한다")
    void findAllByNameIn_Empty_Test() {
        // When
        List<Tag> results = tagRepository.findAllByNameIn(List.of("C++", "Docker"));

        // Then
        assertThat(results).isEmpty();
    }
}