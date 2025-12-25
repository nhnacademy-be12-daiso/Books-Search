package com.daisobook.shop.booksearch.BooksSearch.controller;

import com.daisobook.shop.booksearch.BooksSearch.dto.response.category.CategoryList;
import com.daisobook.shop.booksearch.BooksSearch.service.category.CategoryV2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/books")
public class CategoryV2Controller {
    private final CategoryV2Service categoryService;

    @GetMapping("/categories/all")
    public CategoryList getAllCategoryList(){
        return categoryService.getCategoryList();
    }

}
