package com.daisobook.shop.booksearch.BooksSearch.repository.author;

import com.daisobook.shop.booksearch.BooksSearch.entity.author.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Long> {
    boolean existsRoleByName(String name);

    Role findRoleByName(String name);

    List<Role> findAllByNameIn(Collection<String> names);

    Role findRoleById(long id);
}
