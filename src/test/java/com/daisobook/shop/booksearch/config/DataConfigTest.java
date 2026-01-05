package com.daisobook.shop.booksearch.config;

import com.daisobook.shop.booksearch.repository.book.BookRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest  // 전체 컨텍스트를 띄우지 않고 JPA 관련 빈만 로드 (컨트롤러/비동기 빈 무시)
@Import(DataConfig.class) // 테스트 대상 설정 클래스를 직접 로드
@ActiveProfiles("test")
class DataConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("JPA 리포지토리들이 basePackage 설정에 따라 정상적으로 빈으로 등록되었는지 확인한다")
    void jpaRepositories_Scanning_Test() {
        // Given & When: 특정 리포지토리를 직접 가져와봄
        // 프로젝트에 존재하는 실제 JPA 리포지토리 클래스 중 하나를 사용하세요.
        BookRepository bookRepository = applicationContext.getBean(BookRepository.class);

        // Then
        assertThat(bookRepository).isNotNull().isInstanceOf(JpaRepository.class);
    }

    @Test
    @DisplayName("등록된 빈들 중 JpaRepository 인터페이스를 상속받은 빈의 개수를 확인한다")
    void jpaRepository_Count_Test() {
        // When: JpaRepository 타입의 모든 빈을 조회
        Map<String, JpaRepository> jpaBeans = applicationContext.getBeansOfType(JpaRepository.class);

        // Then: 최소한 1개 이상의 JPA 리포지토리가 등록되어 있어야 함
        assertThat(jpaBeans).isNotEmpty();
    }
}