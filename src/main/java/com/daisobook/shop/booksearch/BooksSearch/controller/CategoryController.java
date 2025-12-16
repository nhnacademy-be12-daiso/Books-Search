package com.daisobook.shop.booksearch.BooksSearch.controller;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.CategoryReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.CategoryRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.service.category.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity addCategory(@RequestBody CategoryReqDTO categoryReqDTO){
        categoryService.registerCategory(categoryReqDTO);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/batch")
    public ResponseEntity addCategories(@RequestBody List<CategoryReqDTO> categoryReqDTOs){
        categoryService.registerCategories(categoryReqDTOs);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public List<CategoryRespDTO> getCategoryList(){
        return categoryService.getCategoryList();
    }

    @GetMapping("/{categoryId}")
    public CategoryRespDTO getCategoryById(@PathVariable("categoryId") long categoryId){
        return categoryService.getCategoryById(categoryId);
    }

    @GetMapping("/name-search/{categoryName}")
    public CategoryRespDTO getCategoryByName(@PathVariable("categoryName") String categoryName){
        return categoryService.getCategoryByName(categoryName);
    }

    @GetMapping("/deep-search/top")
    public List<CategoryRespDTO> getCategoriesByTop(){
        return categoryService.getTopCategories();
    }

    @GetMapping("/deep-search/{deep}")
    public List<CategoryRespDTO> getCategoriesByDeep(@PathVariable("deep") int deep){
        return categoryService.getCategoriesByDeep(deep);
    }

    @GetMapping("/after-search-id/{categoryId}")
    public List<CategoryRespDTO> getCategoriesById(@PathVariable("categoryId") long categoryId){
        return categoryService.getSubCategories(categoryId);
    }

    @GetMapping("/after-search-name/{categoryName}")
    public List<CategoryRespDTO> getCategoriesByName(@PathVariable("categoryName") String categoryName){
        return categoryService.getSubCategories(categoryName);
    }

    @PatchMapping("/{categoryId}")
    public ResponseEntity updateCategory(@PathVariable("categoryId") long categoryId
                                        ,@RequestBody CategoryReqDTO categoryReqDTO){
        categoryService.updateCategory(categoryId, categoryReqDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity updateCategory(@PathVariable("categoryId") long categoryId){
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok().build();
    }

//    @GetMapping("/getPath/{categoryId}")
//    public ResponseEntity getPath(@PathVariable("categoryId") long categoryId){
//        categoryService.getCategoryPath(categoryId);
//        return ResponseEntity.ok().build();
//    }
}
