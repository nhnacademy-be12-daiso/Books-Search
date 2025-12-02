package com.daisobook.shop.booksearch.BooksSearch.repository;

import com.daisobook.shop.booksearch.BooksSearch.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface RoleRepository extends JpaRepository<Role, Long> {
    boolean existsRoleByName(String name);

    Role findRoleByName(String name);

    List<Role> findAllByNameIn(Collection<String> names);

    Role findRoleById(long id);
}
