package com.daisobook.shop.booksearch.dto.response.category;

import com.daisobook.shop.booksearch.dto.projection.CategoryListProjection;

import java.util.ArrayList;
import java.util.List;

public record CategoryTree(
   long categoryId,
   String name,
   Long preCategoryId,
   int deep,
   List<CategoryTree> children
) {
    public static CategoryTree of(CategoryListProjection categoryList){
        return new CategoryTree(
                categoryList.getCategoryId(),
                categoryList.getCategoryName(),
                categoryList.getPreCategoryId(),
                categoryList.getDeep(),
                new ArrayList<>()
        );
    }
}
