package com.daisobook.shop.booksearch.mapper.category;

import com.daisobook.shop.booksearch.dto.projection.CategoryListProjection;
import com.daisobook.shop.booksearch.dto.response.category.CategoryList;
import com.daisobook.shop.booksearch.dto.response.category.CategoryRespDTO;
import com.daisobook.shop.booksearch.dto.response.category.CategoryTree;
import com.daisobook.shop.booksearch.entity.category.BookCategory;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;
import java.util.Map;

public interface CategoryMapper {
    List<CategoryRespDTO> toCategoryRespDTOList(List<BookCategory> bookCategories);
    List<CategoryRespDTO> toCategoryRespDTOList(String categoryData) throws JsonProcessingException;
    Map<Long, List<CategoryRespDTO>> toCategoryRespDTOMap(Map<Long, String> categoryDataMap) throws JsonProcessingException;
    CategoryList toCategoryList(Map<Long, CategoryListProjection> categoryListProjectionMap, List<CategoryListProjection> categoryLeafList);
    List<CategoryTree> toCategoryTreeList(List<CategoryListProjection> categoryProjections);
}
