package com.daisobook.shop.booksearch.BooksSearch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/*
    `category_id`	BIGINT	NOT NULL,
	`category_name`	VARCHAR(20)	NOT NULL,
	`pre_category_id`	BIGINT	NULL	COMMENT '최상위 카테고리는 null'
*/

@Entity
@Table(name = "Categories")
public class Category {
    @Id
    private long id;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "pre_category_id")
    private long preId;
}
