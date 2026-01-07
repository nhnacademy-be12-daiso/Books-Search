package com.daisobook.shop.booksearch.books_search.entity.category;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.util.List;

@NoArgsConstructor
@Entity
@Getter
@Table(name = "categories")
public class Category {

    @Id
    @Column(name="category_id")
    private long id;

    @Setter
    @Column(name = "category_name")
    private String name;

    @Setter
    @Column(name = "deep")
    private int deep;

    @Setter
    @ManyToOne
    @JoinColumn(name = "pre_category_id")
    private Category preCategory;

    public Category(long id, String name, int deep){
        if(id == 0 || name == null || deep <= 0){
            throw new IllegalArgumentException("null");
        }

        this.id = id;
        this.name = name;
        this.deep = deep;
    }

    @OneToMany(mappedBy = "preCategory")
    @BatchSize(size = 100)
    private List<Category> afterCategories;

    @OneToMany(mappedBy = "category")
    @BatchSize(size = 100)
    private List<BookCategory> bookCategories;

}
