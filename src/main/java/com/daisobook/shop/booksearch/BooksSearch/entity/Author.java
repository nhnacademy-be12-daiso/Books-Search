package com.daisobook.shop.booksearch.BooksSearch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "Authors")
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
    }

    @Setter
    @OneToMany(mappedBy = "author")
    private List<BookAuthor> bookAuthors;
}
