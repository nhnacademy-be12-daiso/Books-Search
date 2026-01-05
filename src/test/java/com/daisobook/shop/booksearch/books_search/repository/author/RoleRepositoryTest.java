package com.daisobook.shop.booksearch.books_search.repository.author;

import com.daisobook.shop.booksearch.books_search.dto.projection.RoleNameProjection;
import com.daisobook.shop.booksearch.books_search.entity.author.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        roleRepository.save(new Role("AUTHOR"));
        roleRepository.save(new Role("TRANSLATOR"));
        roleRepository.save(new Role("ILLUSTRATOR"));
    }

    @Test
    @DisplayName("역할 이름 목록으로 역할을 조회한다")
    void findAllByNameInTest() {
        List<Role> result = roleRepository.findAllByNameIn(List.of("AUTHOR", "TRANSLATOR"));

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Role::getName)
                .containsExactlyInAnyOrder("AUTHOR", "TRANSLATOR");
    }

    @Test
    @DisplayName("Native Query를 통해 모든 역할 이름을 프로젝션으로 조회한다")
    void getAllRoleNameTest() {
        List<RoleNameProjection> result = roleRepository.getAllRoleName();

        assertThat(result).isNotEmpty();
        // Projection 인터페이스의 메서드가 getRoleName()이라고 가정
        assertThat(result.getFirst().getRoleName()).isIn("AUTHOR", "TRANSLATOR", "ILLUSTRATOR");
    }
}