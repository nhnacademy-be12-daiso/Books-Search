package com.daisobook.shop.booksearch.mapper.category.impl;

import com.daisobook.shop.booksearch.dto.projection.CategoryListProjection;
import com.daisobook.shop.booksearch.dto.response.category.CategoryList;
import com.daisobook.shop.booksearch.dto.response.category.CategoryPath;
import com.daisobook.shop.booksearch.dto.response.category.CategoryRespDTO;
import com.daisobook.shop.booksearch.dto.response.category.CategoryTree;
import com.daisobook.shop.booksearch.entity.category.BookCategory;
import com.daisobook.shop.booksearch.exception.custom.category.NotFoundCategoryId;
import com.daisobook.shop.booksearch.mapper.category.CategoryMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
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
    public List<CategoryRespDTO> toCategoryRespDTOList(String categoryData) throws JsonProcessingException {
        if(categoryData == null || categoryData.isBlank()){
            return null;
        }

        return objectMapper.readValue(categoryData, new TypeReference<List<CategoryRespDTO>>() {});
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

    @Override
    public CategoryList toCategoryList(Map<Long, CategoryListProjection> categoryListProjectionMap, List<CategoryListProjection> categoryLeafList) {
        List<CategoryPath> categoryPathList = new ArrayList<>();
        for(CategoryListProjection c : categoryLeafList){
            CategoryListProjection current = c;
            StringBuilder path = new StringBuilder();
            while (true) {
                path.insert(0, " > %s:%s".formatted(current.getCategoryId(), current.getCategoryName()));
                if(current.getPreCategoryId() == null){
                    break;
                }
                current = categoryListProjectionMap.get(current.getPreCategoryId());
            }

            categoryPathList.add(new CategoryPath(c.getCategoryId(), c.getCategoryName(), path.toString().trim()));
        }

        return new CategoryList(categoryPathList);
    }

    @Override
    public List<CategoryTree> toCategoryTreeList(List<CategoryListProjection> categoryProjections) {
        Map<Long, CategoryTree> categoryTreeRespDTOMap = categoryProjections.stream()
                .map(CategoryTree::of)
                .collect(Collectors.toMap(CategoryTree::categoryId, categoryTreeRespDTO -> categoryTreeRespDTO));

        List<CategoryTree> treeCategory = new ArrayList<>();
        for(CategoryTree ct : categoryTreeRespDTOMap.values()){
            if(ct.preCategoryId() != null){
                if(!categoryTreeRespDTOMap.containsKey(ct.preCategoryId())){
                    log.error("[카테고리 트리] 해당 카테고리의 상위 카테고리가 관계 불일치 - 카테고리ID:{}, 연결하고자 하는 상위 카테고리ID:{}", ct.categoryId(), ct.preCategoryId());
                    throw new NotFoundCategoryId("[카테고리 트리] 해당 카테고리의 상위 카테고리가 관계 불일치");
                }
                CategoryTree pre = categoryTreeRespDTOMap.get(ct.preCategoryId());
                pre.children().add(ct);
            } else {
                treeCategory.add(ct);
            }
        }

        return treeCategory;
    }
}
