package com.daisobook.shop.booksearch.books_search.entity.author;

import com.daisobook.shop.booksearch.books_search.dto.request.AuthorReqDTO;
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
@Table(name = "authors")
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "author_id")
    private long id;

    @Setter
    @Column(name = "author_name")
    private String name;

    public Author(String name){
        this.name = name;
        this.bookAuthors = new ArrayList<>();
    }

    public static Author create(AuthorReqDTO dto){
        return new Author(dto.roleName());
    }

    @Setter
    @OneToMany(mappedBy = "author")
    @BatchSize(size = 100)
    private List<BookAuthor> bookAuthors;
}
