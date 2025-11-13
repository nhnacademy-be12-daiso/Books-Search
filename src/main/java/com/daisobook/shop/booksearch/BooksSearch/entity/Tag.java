package com.daisobook.shop.booksearch.BooksSearch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/*
	`tag_id`	BIGINT	NOT NULL,
	`tag_name`	VARCHAR(20)	NOT NULL
*/

@Entity
@Getter
@Table(name = "Tags")
public class Tag {

    @Id
    @Column(name = "tag_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long Id;

    @Setter
    @Column(name="tag_name")
    private String name;
}
