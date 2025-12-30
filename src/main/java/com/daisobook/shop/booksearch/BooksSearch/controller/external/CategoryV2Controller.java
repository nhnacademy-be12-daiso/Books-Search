package com.daisobook.shop.booksearch.BooksSearch.controller.external;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.category.CategoryModifyReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.category.CategoryRegisterReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.category.CategoryList;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.category.CategoryTreeListRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.service.category.CategoryV2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/books")
public class CategoryV2Controller {
    private final CategoryV2Service categoryService;

    @GetMapping("/categories/all")
    public CategoryList getAllCategoryList(){
        return categoryService.getCategoryList();
    }

    @GetMapping("/categories/tree")
    public CategoryTreeListRespDTO getCategoryTreeList(){
        return categoryService.getCategoryTreeList();
    }

    @PostMapping("/categories")
    public ResponseEntity postCategory(@RequestBody CategoryRegisterReqDTO categoryRegisterReqDTO) {
        categoryService.registerCategory(categoryRegisterReqDTO);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/categories/{categoryId}")
    public ResponseEntity modifyCategory(@PathVariable("categoryId") long categoryId,
                                         @RequestBody CategoryModifyReqDTO categoryModifyReqDTO){
        categoryService.modifyCategory(categoryId, categoryModifyReqDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/categories/{categoryId}")
    public ResponseEntity deleteCategory(@PathVariable("categoryId") long categoryId){
        return ResponseEntity.ok().build();
    }

}
