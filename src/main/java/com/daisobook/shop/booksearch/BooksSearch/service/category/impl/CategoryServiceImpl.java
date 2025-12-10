package com.daisobook.shop.booksearch.BooksSearch.service.category.impl;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.CategoryReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.CategoryRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.category.BookCategory;
import com.daisobook.shop.booksearch.BooksSearch.entity.category.Category;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.category.*;
import com.daisobook.shop.booksearch.BooksSearch.repository.category.BookCategoryRepository;
import com.daisobook.shop.booksearch.BooksSearch.repository.category.CategoryRepository;
import com.daisobook.shop.booksearch.BooksSearch.service.category.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final BookCategoryRepository bookCategoryRepository;

    @Override
    public void validateNotExistsByName(String categoryName) {
        if(categoryRepository.existsCategoryByName(categoryName)){
            log.error("이미 존재하는 카테고리: {}", categoryName);
            throw new DuplicatedCategory("이미 존재하는 카테고리가 입니다.");
        }
    }

    @Override
    public void validateExistsById(long categoryId) {
        if(!categoryRepository.existsCategoryById(categoryId)){
            log.error("존재하지 않은 카테고리ID: {}", categoryId);
            throw new NotFoundCategoryId("존재하지 않은 카테고리ID 입니다.");
        }
    }

    @Override
    public void validateExistsByName(String categoryName) {
        if(!categoryRepository.existsCategoryByName(categoryName)){
            log.error("존재하지 않은 카테고리 이름: {}", categoryName);
            throw new NotFoundCategoryName("존재하지 않은 카테고리 이름 입니다.");
        }
    }

    @Override
    @Transactional
    public void registerCategory(CategoryReqDTO categoryReqDTO) {
        validateNotExistsByName(categoryReqDTO.categoryName());

        saveCategory(categoryReqDTO);
    }

    @Override
    @Transactional
    public void registerCategories(List<CategoryReqDTO> categoryReqDTOList) {
        Set<String> existCategoryName = categoryRepository.findAllByNameIn(categoryReqDTOList.stream()
                        .map(CategoryReqDTO::categoryName)
                        .toList()).stream()
                .map(Category::getName)
                .collect(Collectors.toSet());

        for(CategoryReqDTO c: categoryReqDTOList){
            if(existCategoryName.contains(c.categoryName())){
                log.error("이미 존재하는 카테고리가 등록 시도 - 카테고리 이름: {}", c.categoryName());
                continue;
            }

            saveCategory(c);
        }
    }

    private void saveCategory(CategoryReqDTO dto){
        Category newCategory = Category.create(dto);
        if(dto.preCategoryName() != null && !dto.preCategoryName().isBlank()){
            Category preCategory = categoryRepository.findCategoryByName(dto.preCategoryName());
            if(preCategory == null){
                log.error("해당 이름을 가진 상위 카테고리를 찾지 못했습니다 - 카테고리 Name:{} ", dto.preCategoryName());
                throw new NotFoundCategoryName("해당 이름을 가진 상위 카테고리를 찾지 못했습니다");
            }

            if(preCategory.getDeep()+1 != dto.deep()){
                log.error("해당 단계가 맞지 않습니다 - 상위 카테고리 Name:{}, 상위 카테고리 단계:{}, 해당 카테고리 Name:{}, 해당 카테고리 단계:{}",
                        preCategory.getName(), preCategory.getDeep(), newCategory.getName(), newCategory.getDeep());
                throw new IllegalArgumentException("연결하고자 하는 상위 카테고리와 단계가 맞지 않습니다");
            }

            newCategory.setPreCategory(preCategory);
            preCategory.getAfterCategories().add(newCategory);
        }

        categoryRepository.save(newCategory);
        log.debug("카테고리 등록 - 카테고리 이름: {}, 카테고리 단계: {}, 상위 카테고리: {}", newCategory.getName(), newCategory.getDeep(),
                newCategory.getAfterCategories() != null ? newCategory.getPreCategory().getName() : "없음");
    }

    @Override
    @Transactional
    public CategoryRespDTO getCategoryById(long categoryId) {
        validateExistsById(categoryId);

        return createCategoryReqDTO(categoryRepository.findCategoryById(categoryId));
    }

    @Override
    @Transactional
    public CategoryRespDTO getCategoryByName(String categoryName) {
        validateExistsByName(categoryName);

        return createCategoryReqDTO(categoryRepository.findCategoryByName(categoryName));
    }

    @Override
    public List<CategoryRespDTO> getTopCategories() {
        return getCategoriesByDeep(1);
    }

    @Override
    public List<CategoryRespDTO> getCategoriesByDeep(int deep) {
        List<Category> categories = categoryRepository.findAllByDeep(deep);
        return categories.stream()
                .map(c -> new CategoryRespDTO(c.getId(), c.getName(), c.getDeep(),
                        c.getPreCategory() != null ? c.getPreCategory().getId() : null,
                        c.getPreCategory() != null ? c.getPreCategory().getName() : null))
                .toList();
    }

    private CategoryRespDTO createCategoryReqDTO(Category category){
        return new CategoryRespDTO(category.getId(), category.getName(), category.getDeep(),
                category.getPreCategory() != null ? category.getPreCategory().getId() : null,
                category.getPreCategory() != null ? category.getPreCategory().getName() : null);
    }

    @Override
    @Transactional
    public List<CategoryRespDTO> getSubCategories(long categoryId) {
        return createCategoryRespDTOs(categoryRepository.findAllByPreCategory_Id(categoryId));
    }

    @Override
    @Transactional
    public List<CategoryRespDTO> getSubCategories(String categoryName) {
        return createCategoryRespDTOs(categoryRepository.findAllByPreCategory_Name(categoryName));
    }

    private List<CategoryRespDTO> createCategoryRespDTOs(List<Category> categories){
        return categories.stream()
                .map(c -> new CategoryRespDTO(c.getId(), c.getName(), c.getDeep(),
                        c.getPreCategory() != null ? c.getPreCategory().getId() : null,
                        c.getPreCategory() != null ? c.getPreCategory().getName() : null))
                .toList();
    }

    @Override
    @Transactional
    public void updateCategory(long categoryId, CategoryReqDTO categoryReqDTO) {
        validateExistsById(categoryId);

        Category category = categoryRepository.findCategoryById(categoryId);

        boolean differentCategoryName = !category.getName().equals(categoryReqDTO.categoryName());

        if(differentCategoryName){
            validateNotExistsByName(categoryReqDTO.categoryName());
            category.setName(categoryReqDTO.categoryName());
        }

        if(category.getAfterCategories() != null){
            log.error("해당 카테고리의 하위 카테고리가 존재하여 단계와 상위 카테고리를 변경을 할 수 없습니다 - 카테고리ID: {}, 카테고리Name: {}, 하위 카테고리 수: {}",
                    category.getId(), category.getName(), category.getAfterCategories().size());
            throw new CannotChangedCategory("해당 카테고리의 하위 카테고리가 존재하여 변경이 불가한 카테고리입니다.");
        }

        boolean differentCategoryDeep = category.getDeep() != categoryReqDTO.deep();
        boolean differentPreCategoryName = !categoryReqDTO.preCategoryName().equals(category.getPreCategory() != null ? category.getPreCategory().getName() : null);


        if (differentPreCategoryName) {
            Category preCategory = category.getPreCategory();
            Category modifyPreCategory = null;

            if(!categoryReqDTO.preCategoryName().isBlank()) {
                modifyPreCategory = categoryRepository.findCategoryByName(categoryReqDTO.preCategoryName());
                if (modifyPreCategory.getDeep() + 1 != categoryReqDTO.deep()) {
                    log.error("바꾸고자 하는 상위 카테고리랑 단계가 불일치 - 상위 카테고리ID: {}, 상위 카테고리 단계:{}, 바꿀 카테고리ID: {}, 변경 할 카테고리 단계:{}",
                            modifyPreCategory.getName(), modifyPreCategory.getDeep(), category.getName(), categoryReqDTO.deep());
                    throw new CannotChangedCategory("변경 하고자 하는 단계가 일치 하지 않습니다.");
                }
            }

            if(preCategory != null) {
                preCategory.getAfterCategories().remove(category);
            }

            if(modifyPreCategory != null) {
                modifyPreCategory.getAfterCategories().add(category);
            }

            category.setPreCategory(modifyPreCategory);

            if (modifyPreCategory != null) {
                category.setDeep(modifyPreCategory.getDeep() + 1);
            } else {
                category.setDeep(1);
            }
        }
        if(differentCategoryDeep && !differentPreCategoryName){
            log.error("상위 카테고리 유지 상태에서 단계 변경 시도 - ID: {}", categoryId);
            throw new CannotChangedCategory("상위 카테고리가 유지되면 단계는 변경될 수 없습니다.");
        }
        if(!differentCategoryName && !differentCategoryDeep && !differentPreCategoryName){
            log.debug("변경사항이 없습니다.");
        }
    }

    @Override
    @Transactional
    public void deleteCategory(long categoryId) {
        validateExistsById(categoryId);
        if(bookCategoryRepository.existsBookCategoriesByCategory_Id(categoryId)){
            log.error("해당 카테고리에 관계가 존재하여 삭제 불가 - 카테고리ID: {}", categoryId);
            throw new CannotChangedCategory("해당 카테고리에 관계가 존재하여 삭제 불가");
        }

        Category category = categoryRepository.findCategoryById(categoryId);
        if(category.getAfterCategories() != null && !category.getAfterCategories().isEmpty()){
            log.error("해당 카테고리의 하위 카테고리가 존재하여 삭제 불가 - 카테고리ID: {}, 카테고리Name: {}, 하위 카테고리 수: {}",
                    category.getId(), category.getName(), category.getAfterCategories().size());
            throw new CannotChangedCategory("해당 카테고리의 하위 카테고리가 존재하여 삭제 불가");
        }

        categoryRepository.delete(category);
    }

    //bookService에서 사용되는 메서드

    @Override
    public Category findValidCategoryByNameAndDeep(String categoryName, int deep) {
        Category category = categoryRepository.findCategoryByNameAndDeep(categoryName, deep);

        if(category == null){
            log.error("존재하지 않는 카테고리 도서 등록 시도 - category name: {}, deep: {}", categoryName, deep);
            throw new NotFoundBookCategory("존재하지 않는 카테고리 입니다");
        }
        return category;
    }

    @Override
    public List<Category> findCategoriesByIds(List<Long> categoryIds) {
        return categoryRepository.findAllByIdIn(categoryIds);
    }

    @Override
    public List<CategoryRespDTO> getCategoryDTOsByIds(List<Long> categoryIds) {
        List<Category> categories = findCategoriesByIds(categoryIds);

        return categories.stream()
                .map(c -> new CategoryRespDTO(c.getId(), c.getName(), c.getDeep(),
                        c.getPreCategory() != null ? c.getPreCategory().getId() : null,
                        c.getPreCategory() != null ? c.getPreCategory().getName() : null))
                .toList();
    }

    @Override
    public List<Category> findCategoriesByNamesAndDeeps(List<String> categoryNames, List<Integer> deeps) {
        return categoryRepository.findAllByNameInAndDeepIn(categoryNames, deeps);
    }

    @Override
    public List<Category> findAllByBookCategories(List<BookCategory> bookCategories) {
        return categoryRepository.findAllByBookCategories(bookCategories);
    }

    @Override
    public Category findCategoryByName(String categoryName){
        return categoryRepository.findCategoryByName(categoryName);
    }
}
