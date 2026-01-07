package com.daisobook.shop.booksearch.books_search.repository.author;

import com.daisobook.shop.booksearch.books_search.dto.projection.RoleNameProjection;
import com.daisobook.shop.booksearch.books_search.entity.author.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Long> {

    List<Role> findAllByNameIn(Collection<String> names);

    @Query(value = """
            SELECT
            r.role_name as roleName
            FROM roles r
            """,
            nativeQuery = true)
    List<RoleNameProjection> getAllRoleName();
}
