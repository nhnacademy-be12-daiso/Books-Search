package com.daisobook.shop.booksearch.BooksSearch.repository.author;

import com.daisobook.shop.booksearch.BooksSearch.dto.projection.RoleNameProjection;
import com.daisobook.shop.booksearch.BooksSearch.entity.author.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Long> {
    boolean existsRoleByName(String name);

    Role findRoleByName(String name);

    List<Role> findAllByNameIn(Collection<String> names);

    Role findRoleById(long id);

    @Query(value = """
            SELECT
            r.role_name as roleName
            FROM roles r
            """,
            nativeQuery = true)
    List<RoleNameProjection> getAllRoleName();
}
