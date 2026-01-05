package com.daisobook.shop.booksearch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "book_of_the_week" , uniqueConstraints = {@UniqueConstraint(columnNames = {"applied_date", "no"})})
public class BookOfTheWeek {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "no")
    private int no;

    @Column(name = "book_id")
    private long bookId;

    @Setter
    @Column(name = "reason")
    private String reason;

    @Column(name = "applied_date", nullable = false)
    private ZonedDateTime appliedDate;

    @Column(name = "created_date", nullable = false)
    private ZonedDateTime createdDate;

    @Column(name = "updated_date")
    private ZonedDateTime zonedDateTime;

    @Setter
    @Column(name = "is_active")
    private boolean isActive = true;

    public BookOfTheWeek(int no, long bookId, String reason, ZonedDateTime appliedDate){
        this.no = no;
        this.bookId = bookId;
        this.reason = reason;
        this.appliedDate = appliedDate;
        this.createdDate = ZonedDateTime.now();
    }
}
