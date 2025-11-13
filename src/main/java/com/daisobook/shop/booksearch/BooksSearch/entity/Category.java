package com.daisobook.shop.booksearch.BooksSearch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/*
    `category_id`	BIGINT	NOT NULL,
	`category_name`	VARCHAR(20)	NOT NULL,
	`pre_category_id`	BIGINT	NULL	COMMENT '최상위 카테고리는 null'
*/

@Entity
@Getter
@Table(name = "Categories")
public class Category {

    @Id
    @Column(name="category_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Setter
    @Column(name = "category_name")
    private String categoryName;

    @Setter
    @Column(name = "pre_category_id")
    private long preId;
}
