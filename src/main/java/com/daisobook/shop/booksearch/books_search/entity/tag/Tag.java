package com.daisobook.shop.booksearch.books_search.entity.tag;

import com.daisobook.shop.booksearch.books_search.dto.request.TagReqDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Entity
@Getter
@Table(name = "tags")
public class Tag {

    @Id
    @Column(name = "tag_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Setter
    @Column(name="tag_name", unique = true)
    private String name;

    public Tag(String name){
        this.name = name;
        this.bookTags = new ArrayList<>();
    }

    public static Tag create(TagReqDTO tagReqDTO){
        return new Tag(tagReqDTO.tagName());
    }

    @OneToMany(mappedBy = "tag")
    @BatchSize(size = 100)
    private List<BookTag> bookTags;
}
