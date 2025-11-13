package com.daisobook.shop.booksearch.BooksSearch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Setter
    @Column(name="isbn", nullable = false, length = 17)
    private String isbn;

    @Setter
    @Column(name="title", nullable = false, length = 100)
    private String title;

    @Setter
    @Column(name="contents", columnDefinition = "TEXT")
    private String contents;

    @Setter
    @Column(name="description", columnDefinition = "TEXT")
    private String description;

    @Setter
    @Column(name="author", length = 60)
    private String author;

    @Setter
    @Column(name="publisher", length = 60)
    private String publisher;

    @Setter
    @Column(name="publication_date")
    private ZonedDateTime publication_date;

    @Setter
    @Column(name="price", nullable = false)
    private int price;

    @Setter
    @Column(name="is_packaging")
    private boolean isPackaging;

    @Column(name="stock", nullable = false)
    private int stock;

    @Enumerated(EnumType.STRING)
    @Column(name="status", columnDefinition = "ENUM('DISCONTINUE', 'ON_SALE') DEFAULT 'ON_SALE'")
    private Status status;
}
