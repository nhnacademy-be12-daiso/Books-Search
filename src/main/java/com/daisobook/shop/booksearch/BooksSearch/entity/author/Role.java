package com.daisobook.shop.booksearch.BooksSearch.entity.author;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.AuthorReqDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private long id;

    @Setter
    @Column(name = "role_name")
    private String name;

    public Role(String name){
        this.name = name;
        this.bookAuthors = new ArrayList<>();
    }

    public static Role create(AuthorReqDTO dto){
        return new Role(dto.roleName());
    }

    @Setter
    @OneToMany(mappedBy = "role")
    @BatchSize(size = 100)
    private List<BookAuthor> bookAuthors;
}
