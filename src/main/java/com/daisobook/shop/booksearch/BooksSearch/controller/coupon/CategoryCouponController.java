package com.daisobook.shop.booksearch.BooksSearch.controller.coupon;

import com.daisobook.shop.booksearch.BooksSearch.dto.coupon.response.BookCategoryResponse;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.coupon.CategorySimpleResponse;
import com.daisobook.shop.booksearch.BooksSearch.entity.category.Category;
import com.daisobook.shop.booksearch.BooksSearch.repository.category.CategoryRepository;
import com.daisobook.shop.booksearch.BooksSearch.service.book.impl.BookCoreService;
import com.daisobook.shop.booksearch.BooksSearch.service.category.CategoryV2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class CategoryCouponController {

    private final CategoryRepository categoryRepository;
    private final CategoryV2Service categoryService;

    @GetMapping("/categoriesIds")
    public List<CategorySimpleResponse> getCategoriesByIds(
            @RequestParam("ids") List<Long> categoryIds
    ) {
        List<Category> categories = categoryRepository.findByIdIn(categoryIds);

        return categories.stream()
                .map(cat -> new CategorySimpleResponse(
                        cat.getId(),
                        cat.getName()   // Category 엔티티의 필드명에 맞게
                ))
                .toList();
    }

    @GetMapping("{bookId}/category")
    public BookCategoryResponse getBookCategory(@PathVariable Long bookId){
        return categoryService.bookCategory(bookId);

    }
}