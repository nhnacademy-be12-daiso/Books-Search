package com.daisobook.shop.booksearch.controller.external;

import com.daisobook.shop.booksearch.controller.docs.CategoryV2ControllerDocs;
import com.daisobook.shop.booksearch.dto.request.category.CategoryModifyReqDTO;
import com.daisobook.shop.booksearch.dto.request.category.CategoryRegisterReqDTO;
import com.daisobook.shop.booksearch.dto.response.category.CategoryList;
import com.daisobook.shop.booksearch.dto.response.category.CategoryTreeListRespDTO;
import com.daisobook.shop.booksearch.service.category.CategoryV2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/books")
public class CategoryV2Controller implements CategoryV2ControllerDocs {
    private final CategoryV2Service categoryService;

    @GetMapping("/categories")
    public CategoryList getAllCategoryList(){
        return categoryService.getCategoryList();
    }

    @GetMapping("/categories/tree")
    public CategoryTreeListRespDTO getCategoryTreeList(){
        return categoryService.getCategoryTreeList();
    }

    @PostMapping("/categories")
    public ResponseEntity<Void> postCategory(@RequestBody CategoryRegisterReqDTO categoryRegisterReqDTO) {
        categoryService.registerCategory(categoryRegisterReqDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/categories/{categoryId}")
    public ResponseEntity<Void> modifyCategory(@PathVariable("categoryId") long categoryId,
                                         @RequestBody CategoryModifyReqDTO categoryModifyReqDTO){
        categoryService.modifyCategory(categoryId, categoryModifyReqDTO);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/categories/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable("categoryId") long categoryId){
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

}
