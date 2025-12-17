package com.daisobook.shop.booksearch.BooksSearch.mapper.category;

import com.daisobook.shop.booksearch.BooksSearch.dto.response.CategoryRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.category.BookCategory;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;
import java.util.Map;

public interface CategoryMapper {
    List<CategoryRespDTO> toCategoryRespDTOList(List<BookCategory> bookCategories);
    Map<Long, List<CategoryRespDTO>> toCategoryRespDTOMap(Map<Long, String> categoryDataMap) throws JsonProcessingException;
}
