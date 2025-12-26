package com.daisobook.shop.booksearch.BooksSearch.service.category.impl;

import com.daisobook.shop.booksearch.BooksSearch.dto.projection.CategoryListProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.CategoryPathProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.category.CategoryList;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.entity.category.BookCategory;
import com.daisobook.shop.booksearch.BooksSearch.entity.category.Category;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.category.NotFoundCategoryId;
import com.daisobook.shop.booksearch.BooksSearch.mapper.category.CategoryMapper;
import com.daisobook.shop.booksearch.BooksSearch.repository.category.BookCategoryRepository;
import com.daisobook.shop.booksearch.BooksSearch.repository.category.CategoryRepository;
import com.daisobook.shop.booksearch.BooksSearch.service.category.CategoryV2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class CategoryV2ServiceImpl implements CategoryV2Service {

    private final CategoryRepository categoryRepository;
    private final BookCategoryRepository bookCategoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public void assignCategoriesToBook(Book book, Long categoryId) {
        if(categoryId == null){
            log.error("[도서 등록] category Id가 null입니다 - 해당 도서 ISBN: {}", book.getIsbn());
            return;
        }

        List<CategoryPathProjection> findCategoryIds = categoryRepository.findAncestorsPathByFinalCategoryId(categoryId);
        if(findCategoryIds == null || findCategoryIds.isEmpty()){
            log.error("[도서 등록] 해당 카테고리를 찾지 못했습니다 - category ID: {}", categoryId);
            throw new NotFoundCategoryId("[도서 등록] 해당 카테고리를 찾지 못했습니다.");
        }

        List<Category> categoryList = categoryRepository.findAllByIdIn(findCategoryIds.stream()
                .map(CategoryPathProjection::getId)
                .toList());

        List<BookCategory> bookCategories = new ArrayList<>();
        for(Category c: categoryList) {
            BookCategory newBookCategory = new BookCategory(book, c);

            c.getBookCategories().add(newBookCategory);
            book.getBookCategories().add(newBookCategory);
            log.debug("[도서 등록] 해당 카테고리 관계 생성 - 도서 ID:{}, category ID:{}", book.getId(), c.getId());

            bookCategories.add(newBookCategory);
        }

        bookCategoryRepository.saveAll(bookCategories);
    }

    @Override
    @Transactional
    public void assignCategoriesToBooks(Map<String, Book> bookMap, Map<String, Long> categoryIdMap) {
        Map<Long, CategoryPathProjection> findCategoryIds = categoryRepository.findAncestorsPathByFinalCategoryIdIn(
                new HashSet<>(categoryIdMap.values()).stream()
                        .toList()).stream()
                .collect(Collectors.toMap(CategoryPathProjection::getId, c -> c));

        Map<Long, Category> categoryMap = categoryRepository.findAllByIdIn(findCategoryIds.values().stream()
                        .map(CategoryPathProjection::getId)
                        .toList()).stream()
                .collect(Collectors.toMap(Category::getId, category -> category));

        List<BookCategory> bookCategories = new ArrayList<>();
        for(Book book: bookMap.values()){
            Long finalCategoryId = categoryIdMap.get(book.getIsbn());
            if(finalCategoryId == null || !categoryMap.containsKey(finalCategoryId)){
                log.error("[여러 도서 등록] 해당 카테고리를 찾지 못했습니다 - 해당 도서 ISBN: {} category ID: {}", book.getIsbn(), finalCategoryId);
                continue;
            }

            Long currentId = finalCategoryId;
            while(currentId != null && currentId != 0) {
                CategoryPathProjection path = findCategoryIds.get(currentId);
                if (path == null) break;

                Category c = categoryMap.get(currentId);
                BookCategory newBookCategory = new BookCategory(book, c);

                book.getBookCategories().add(newBookCategory);
                c.getBookCategories().add(newBookCategory);

                bookCategories.add(newBookCategory);

                currentId = path.getPreCategoryId();
            }
        }
        bookCategoryRepository.saveAll(bookCategories);
    }

    @Override
    @Transactional
    public void updateCategoryOfBook(Book book, Long categoryId) {
        List<BookCategory> preBookCategories = book.getBookCategories();

        Set<Long> categoryIdSet = preBookCategories.stream()
                .map(bc -> bc.getCategory().getId())
                .collect(Collectors.toSet());

        if(categoryIdSet.contains(categoryId)){
            log.debug("변경사항 없음");
            return;
        }

        List<CategoryPathProjection> pathProjections = categoryRepository.findAncestorsPathByFinalCategoryId(categoryId);
        if(pathProjections == null || pathProjections.isEmpty()){
            log.error("[도서 수정] 해당 카테고리를 찾지 못했습니다 - category ID: {}", categoryId);
            throw new NotFoundCategoryId("[도서 수정] 해당 카테고리를 찾지 못했습니다.");
        }

        Map<Integer, BookCategory> preBookCategoryMap = preBookCategories.stream()
                .collect(Collectors.toMap(bc -> bc.getCategory().getDeep(), bookCategory -> bookCategory));

        Map<Integer, Category> updateCategoryMap = categoryRepository.findAllByIdIn(pathProjections.stream()
                    .map(CategoryPathProjection::getId)
                    .toList()).stream()
                .collect(Collectors.toMap(Category::getDeep, category -> category));

        Map<Integer, Category> preCategoryMap = preBookCategories.stream()
                    .map(BookCategory::getCategory)
                    .toList().stream()
                .collect(Collectors.toMap(Category::getDeep, category -> category));

        int size = Math.max(updateCategoryMap.size(), preBookCategories.size());

        List<BookCategory> saveBookCategoryList = new ArrayList<>();
        List<Long> deleteBookCategoryIdList = new ArrayList<>();
        for(int i = 1; i <= size; i++){
            Category uc = updateCategoryMap.getOrDefault(i, null);
            Category pc = preCategoryMap.getOrDefault(i, null);
            BookCategory bookCategory = preBookCategoryMap.getOrDefault(i, null);

            if(uc != null && pc != null){
                if(uc.getId() == pc.getId()){
                    continue;
                }
                pc.getBookCategories().remove(bookCategory);
                uc.getBookCategories().add(bookCategory);
                bookCategory.setCategory(uc);

                log.debug("[도서 수정] 변경사항 - 이전 카테고리 - ID: {}, Name: {}, Deep: {}, preCategory: {}", pc.getId(), pc.getName(), pc.getDeep(), pc.getPreCategory().getName());
                log.debug("[도서 수정] 변경사항 - 변경 카테고리 - ID: {}, Name: {}, Deep: {}, preCategory: {}", uc.getId(), uc.getName(), uc.getDeep(), uc.getPreCategory().getName());

            } else if(uc != null || pc != null) {
                if(pc == null){
                    BookCategory newBookCategory = new BookCategory(book, uc);

                    uc.getBookCategories().add(newBookCategory);
                    book.getBookCategories().add(newBookCategory);

                    saveBookCategoryList.add(newBookCategory);
                    log.debug("[도서 수정] 새로운 카테고리 추가 - 추가 카테고리 - ID: {}, Name: {}, Deep: {}, preCategory: {}", uc.getId(), uc.getName(), uc.getDeep(), uc.getPreCategory().getName());

                } else {
                    pc.getBookCategories().remove(bookCategory);
                    book.getBookCategories().remove(bookCategory);

                    deleteBookCategoryIdList.add(bookCategory.getId());
                    log.debug("[도서 수정] 이전 카테고리 삭제- 이전 카테고리 - ID: {}, Name: {}, Deep: {}, preCategory: {}", pc.getId(), pc.getName(), pc.getDeep(), pc.getPreCategory().getName());

                }
            }
        }
        if(!deleteBookCategoryIdList.isEmpty()) {
            bookCategoryRepository.removeAllByIdIn(deleteBookCategoryIdList);
        }
        if(!saveBookCategoryList.isEmpty()) {
            bookCategoryRepository.saveAll(saveBookCategoryList);
        }
    }

    @Override
    public void deleteCategoryOfBook(Book book) {
        List<BookCategory> bookCategories = book.getBookCategories();
        List<Category> categories = bookCategories.stream().map(BookCategory::getCategory).toList();

        if(!categories.isEmpty()){
            categories.forEach(c -> c.getBookCategories().removeAll(bookCategories));
        }
        book.getBookCategories().removeAll(bookCategories);

        bookCategoryRepository.deleteAll(bookCategories);
    }

    @Override
    public CategoryList getCategoryList() {
        Map<Long, CategoryListProjection> categoryRepositoryAll = categoryRepository.getAll().stream()
                .collect(Collectors.toMap(CategoryListProjection::getCategoryId, c -> c));

        Set<Long> preCategoryIdSet = categoryRepositoryAll.values().stream()
                .map(CategoryListProjection::getPreCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<CategoryListProjection> categoryLeafList = new ArrayList<>();
        for(CategoryListProjection c : categoryRepositoryAll.values()){
            if(!preCategoryIdSet.contains(c.getCategoryId())){
                categoryLeafList.add(c);
            }
        }

        CategoryList categoryList = categoryMapper.toCategoryList(categoryRepositoryAll, categoryLeafList);

        return categoryList;
    }

    @Override
    public Long getCountAll() {
        return categoryRepository.count();
    }
}
