package com.daisobook.shop.booksearch.BooksSearch.controller.coupon;

import com.daisobook.shop.booksearch.BooksSearch.dto.response.coupon.CategorySimpleResponse;
import com.daisobook.shop.booksearch.BooksSearch.entity.category.Category;
import com.daisobook.shop.booksearch.BooksSearch.repository.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class CategoryCouponController {

    private final CategoryRepository categoryRepository;

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
}