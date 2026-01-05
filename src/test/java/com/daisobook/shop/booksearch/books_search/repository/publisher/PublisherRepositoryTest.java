package com.daisobook.shop.booksearch.books_search.repository.publisher;

import com.daisobook.shop.booksearch.books_search.entity.publisher.Publisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PublisherRepositoryTest {

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Publisher savedPublisher;

    @BeforeEach
    void setUp() {
        // 테스트용 출판사 데이터 생성 및 저장
        Publisher publisher = new Publisher("다이소출판");
        this.savedPublisher = entityManager.persistAndFlush(publisher);
        
        // 추가 데이터 (다중 조회를 위함)
        entityManager.persist(new Publisher("공공출판"));
        entityManager.persist(new Publisher("우리출판"));
        
        entityManager.clear(); // 영속성 컨텍스트 초기화
    }

    @Test
    @DisplayName("출판사 이름으로 단건 조회를 수행한다")
    void findPublisherByNameTest() {
        // When
        Publisher result = publisherRepository.findPublisherByName("다이소출판");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("다이소출판");
        assertThat(result.getId()).isEqualTo(savedPublisher.getId());
    }

    @Test
    @DisplayName("여러 출판사 이름 목록으로 조회를 수행한다")
    void findAllByNameInTest() {
        // Given
        List<String> names = List.of("다이소출판", "공공출판", "없는출판");

        // When
        List<Publisher> results = publisherRepository.findAllByNameIn(names);

        // Then
        assertThat(results).hasSize(2); // 존재하는 것만 가져와야 함
        assertThat(results).extracting(Publisher::getName)
                           .containsExactlyInAnyOrder("다이소출판", "공공출판");
    }

    @Test
    @DisplayName("존재하지 않는 이름으로 조회 시 null을 반환한다")
    void findPublisherByName_NotFound_Test() {
        // When
        Publisher result = publisherRepository.findPublisherByName("유령출판사");

        // Then
        assertThat(result).isNull();
    }
}