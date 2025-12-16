package com.daisobook.shop.booksearch.BooksSearch.service.category.impl;

import com.daisobook.shop.booksearch.BooksSearch.dto.projection.CategoryPathProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.CategoryReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.entity.category.BookCategory;
import com.daisobook.shop.booksearch.BooksSearch.entity.category.Category;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.category.NotFoundCategoryId;
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

    @Override
    @Transactional
    public void assignCategoriesToBook(Book book, Long categoryId) {
        if(categoryId == null){
            log.error("[도서 등록] category Id가 null입니다 - 해당 도서 ISBN: {}, 등록시도 카테고리 ID: {}", book.getIsbn(), categoryId);
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
    public void updateCategory(Book book, Long categoryId) {
        List<BookCategory> preBookCategories = book.getBookCategories();

        Set<Long> categoryIdSet = preBookCategories.stream()
                .map(bc -> bc.getCategory().getId())
                .collect(Collectors.toSet());

        if(categoryIdSet.contains(categoryId)){
            log.debug("변경사항 없음");
            return;
        }

//        List<Category> updateCategories = categoryService.findCategoriesByNamesAndDeeps(bookReqDTO.categories().stream().map(CategoryReqDTO::categoryName).toList(),
//                bookReqDTO.categories().stream().map(CategoryReqDTO::deep).toList());
//
//        List<Category> preCategories = preBookCategories.stream().map(BookCategory::getCategory).toList();
//
//        for(Category updateC: updateCategories){
//            for(Category preC: preCategories){
//                if(updateC.getDeep() == preC.getDeep()){
//                    if(updateC.getId() == preC.getId()){
//                        continue;
//                    }
//
//                    log.debug("변경사항 - 이전 카테고리 - ID: {}, Name: {}, Deep: {}, preCategory: {}", preC.getId(), preC.getName(), preC.getDeep(), preC.getPreCategory().getName());
//                    log.debug("변경사항 - 변경 카테고리 - ID: {}, Name: {}, Deep: {}, preCategory: {}", updateC.getId(), updateC.getName(), updateC.getDeep(), updateC.getPreCategory().getName());
//
//                    for(BookCategory bc: preBookCategories){
//                        if(bc.getCategory().getId() == preC.getId()){
//                            preC.getBookCategories().removeIf(preBc -> preBc.getId() == bc.getId());
//                            bc.setCategory(updateC);
//                            updateC.getBookCategories().add(bc);
//                            break;
//                        }
//                    }
//                    break;
//                }
//            }
//        }
    }
}
