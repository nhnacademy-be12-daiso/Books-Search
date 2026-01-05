package com.daisobook.shop.booksearch.books_search.controller.internal.coupon;

import com.daisobook.shop.booksearch.books_search.controller.docs.CategoryCouponControllerDocs;
import com.daisobook.shop.booksearch.books_search.dto.coupon.response.BookCategoryResponse;
import com.daisobook.shop.booksearch.books_search.dto.response.coupon.CategorySimpleResponse;
import com.daisobook.shop.booksearch.books_search.service.category.CategoryV2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class CategoryCouponController implements CategoryCouponControllerDocs {

    private final CategoryV2Service categoryService;

    @GetMapping("/categoriesIds")
    public List<CategorySimpleResponse> getCategoriesByIds(
            @RequestParam("ids") List<Long> categoryIds
    ) {
        return categoryService.findByIdIn(categoryIds);
    }

    @GetMapping("{bookId}/category")
    public BookCategoryResponse getBookCategory(@PathVariable Long bookId){
        return categoryService.bookCategory(bookId);

    }
}