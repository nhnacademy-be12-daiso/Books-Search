package com.daisobook.shop.booksearch.BooksSearch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/*
	`tag_id`	BIGINT	NOT NULL,
	`tag_name`	VARCHAR(20)	NOT NULL
*/

@Entity
@Table(name = "Tags")
public class Tag {

    @Id
    @Column(name = "tag_id")
    private long Id;

    private String name;
}
