package com.daisobook.shop.booksearch.BooksSearch.mapper.category.impl;

import com.daisobook.shop.booksearch.BooksSearch.dto.response.CategoryRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.category.BookCategory;
import com.daisobook.shop.booksearch.BooksSearch.mapper.category.CategoryMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class CategoryMapperImpl implements CategoryMapper {
    private final ObjectMapper objectMapper;

    @Override
    public List<CategoryRespDTO> toCategoryRespDTOList(List<BookCategory> bookCategories) {
        return bookCategories.stream()
                .map(BookCategory::getCategory)
                .map(c -> new CategoryRespDTO(c.getId(), c.getName(), c.getDeep(),
                        c.getPreCategory() != null ? c.getPreCategory().getId() : null,
                        c.getPreCategory() != null ? c.getPreCategory().getName() : null))
                .toList();
    }

    @Override
    public Map<Long, List<CategoryRespDTO>> toCategoryRespDTOMap(Map<Long, String> categoryDataMap) throws JsonProcessingException {
        Set<Long> keySet = categoryDataMap.keySet();

        Map<Long, List<CategoryRespDTO>> listMap = new HashMap<>();
        for(Long key: keySet){
            if(!categoryDataMap.containsKey(key)){
                continue;
            }
            String s = categoryDataMap.get(key);
            List<CategoryRespDTO> categoryRespDTOList = objectMapper.readValue(s, new TypeReference<List<CategoryRespDTO>>() {});
            listMap.put(key, categoryRespDTOList);
        }
        return listMap;
    }
}
