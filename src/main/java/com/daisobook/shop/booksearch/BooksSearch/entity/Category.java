package com.daisobook.shop.booksearch.BooksSearch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
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
    private String name;

    @Setter
    @Column(name = "deep")
    private int deep;

//    @Setter
//    @Column(name = "pre_category_id")
//    private long preId;
    @Setter
    @ManyToOne
    private Category preCategory;

    public Category(String name, int deep, Category preCategory){
        if(name == null || deep == 0 || (deep == 1 && preCategory == null)){
            throw new IllegalArgumentException("null");
        }

        this.name = name;
        this.deep = deep;
        this.preCategory = preCategory;
        this.preCategory.getAfterCategories().add(this);
    }

    @OneToMany(mappedBy = "preCategory")
    private List<Category> afterCategories;

    @OneToMany(mappedBy = "category")
    private List<BookCategory> bookCategories;
}
