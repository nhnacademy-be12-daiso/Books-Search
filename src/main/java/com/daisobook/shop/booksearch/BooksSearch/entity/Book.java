package com.daisobook.shop.booksearch.BooksSearch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/*
    `book_id`	BIGINT	NOT NULL,
	`isbn`	VARCHAR(17)	NOT NULL,
	`title`	VARCHAR2(100)	NOT NULL,
	`contents`	TEXT	NULL,
	`description`	TEXT	NULL,
	`author`	VARCHAR2(60)	NULL,
	`publisher`	VARCHAR(60)	NULL,
	`publication_date`	TIMESTAMP	NULL,
	`price`	INT	NOT NULL,
	`is_packaging`	BOOLEAN	NOT NULL,
	`stock`	INT	NOT NULL,
	`status`	ENUM('DISCONTINUE', 'ON_SALE')	NULL	DEFAULT ON_SALE
*/

@NoArgsConstructor
@Getter
@Entity
@Table(name = "Books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private long id;

    private String isbn;
    private String title;
    private String contents;
    private String author;
    private String publisher;
    private ZonedDateTime publication_date;
    private int price;
    private boolean isPackaging;
    private int stock;
    private Status status;
}
