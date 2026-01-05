package com.daisobook.shop.booksearch.entity.publisher;

import com.daisobook.shop.booksearch.dto.request.PublisherReqDTO;
import com.daisobook.shop.booksearch.entity.book.Book;
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
@Table(name = "publishers")
public class Publisher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "publisher_id")
    private long id;

    @Setter
    @Column(name = "publisher_name")
    private String name;

    public Publisher(String name){
        this.name = name;
        this.bookList = new ArrayList<>();
    }

    public static Publisher create(PublisherReqDTO dto){
        return new Publisher(dto.name());
    }

    @Setter
    @OneToMany(mappedBy = "publisher")
    @BatchSize(size = 100)
    private List<Book> bookList;
}
