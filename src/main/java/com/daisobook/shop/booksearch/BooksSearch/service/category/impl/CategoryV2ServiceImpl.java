package com.daisobook.shop.booksearch.BooksSearch.service.category.impl;

import com.daisobook.shop.booksearch.BooksSearch.dto.coupon.response.BookCategoryResponse;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.CategoryListProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.CategoryPathProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.category.CategoryModifyReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.category.CategoryRegisterReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.category.CategoryList;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.category.CategoryTree;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.category.CategoryTreeListRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.entity.category.BookCategory;
import com.daisobook.shop.booksearch.BooksSearch.entity.category.Category;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.category.ExistedCategory;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.category.InvalidCategoryDepthException;
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

    private boolean existCategory(long id){
        return categoryRepository.existsCategoryById(id);
    }

    @Override
    @Transactional
    public void registerCategory(CategoryRegisterReqDTO reqDTO) {
        if(existCategory(reqDTO.categoryId())){
            log.error("[카테고리 등록] 이미 존재하는 카테고리 아이디 등록입니다 - 카테고리ID:{}", reqDTO.categoryId());
            throw new ExistedCategory("[카테고리 등록] 이미 존재하는 카테고리 아이디 등록입니다");
        }

        Category category = new Category(reqDTO.categoryId(), reqDTO.name(), reqDTO.deep());
        if(reqDTO.preCategoryId() != null) {
            Category preCategory = categoryRepository.findCategoryById(reqDTO.preCategoryId());
            if(preCategory == null){
                log.error("[카테고리 등록] 해당 카테고리랑 연결할 상위 카테고리를 찾지 못하였습니다 - 등록 카테고리 ID:{}, 등록 카테고리 name:{}, 상위 카테고리 ID:{}", reqDTO.categoryId(), reqDTO.name(), reqDTO.preCategoryId());
                throw new NotFoundCategoryId("[카테고리 등록] 해당 카테고리랑 연결할 상위 카테고리를 찾지 못하였습니다");
            }
            if(preCategory.getDeep() >= category.getDeep()){
                log.error("[카테고리 등록] 해당 카테고리에 연결한 상위 카테고리와의 단계가 맞지 않습니다 - 등록 카테고리 단계:{}, 상위 카테고리 단계:{}", category.getDeep(), preCategory.getDeep());
                throw new InvalidCategoryDepthException("[카테고리 등록] 해당 카테고리에 연결한 상위 카테고리와의 단계가 맞지 않습니다");
            }
            category.setPreCategory(preCategory);
        } else {
            if(category.getDeep() != 1){
                log.error("[카테고리 등록] 해당 카테고리 단계가 최상위에 맞지 않는 단계입니다 - 등록 카테고리 단계:{}", category.getDeep());
                throw new InvalidCategoryDepthException("[카테고리 등록] 해당 카테고리 단계가 최상위에 맞지 않는 단계입니다");
            }
        }
        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void modifyCategory(long categoryId, CategoryModifyReqDTO reqDTO) {
        if(!existCategory(categoryId)){
            log.error("[카테고리 수정] 존재하지 않는 카테고리 수정입니다 - 카테고리ID:{}", categoryId);
            throw new ExistedCategory("[카테고리 등록] 존재하지 않는 카테고리 수정입니다");
        }

        Category category = categoryRepository.findCategoryById(categoryId);

        if(reqDTO.name() != null && !reqDTO.name().isBlank()
                && !category.getName().equals(reqDTO.name())){
            category.setName(reqDTO.name());
        }
        if(reqDTO.deep() != category.getDeep()){
            Category preCategory = category.getPreCategory();
            if(preCategory != null && category.getPreCategory().getDeep() >= reqDTO.deep()) {
                log.error("[카테고리 수정] 해당 카테고리에 연결한 상위 카테고리와의 단계가 맞지 않습니다 - 수정 카테고리 단계:{}, 상위 카테고리 단계:{}", category.getDeep(), preCategory.getDeep());
                throw new InvalidCategoryDepthException("[카테고리 수정] 해당 카테고리에 연결한 상위 카테고리와의 단계가 맞지 않습니다");
            }
            category.setDeep(reqDTO.deep());
        }
    }

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

    @Transactional(readOnly = true)
    @Override
    public CategoryTreeListRespDTO getCategoryTreeList() {
        List<CategoryListProjection> all = categoryRepository.getAll();
        List<CategoryTree> categoryTreeList = categoryMapper.toCategoryTreeList(all);
        return new CategoryTreeListRespDTO(categoryTreeList);
    }

    @Transactional
    @Override
    public BookCategoryResponse bookCategory(Long bookId) {
        // 1. 책에 연결된 카테고리 정보 조회
        List<BookCategory> allByBookId = bookCategoryRepository.findAllByBook_Id(bookId);

        if (allByBookId.isEmpty()) {
            return new BookCategoryResponse(bookId, null, null);
        }

        // 변수 초기화
        Long firstCategoryId = null;  // 1단계 (10% 쿠폰용)
        Long secondCategoryId = null; // 2단계 (15% 쿠폰용)

        // 2. 조회된 리스트를 순회
        for (BookCategory bc : allByBookId) {
            Category category = bc.getCategory();
            if (category == null) continue;

            int deep = category.getDeep(); // 단계 확인

            if (deep == 1) {
                // 1단계인 경우
                firstCategoryId = category.getId();

            } else if (deep == 2) {
                // 2단계인 경우 -> 본인은 2단계, 부모는 1단계
                secondCategoryId = category.getId();
                if (firstCategoryId == null && category.getPreCategory() != null) {
                    firstCategoryId = category.getPreCategory().getId();
                }

            } else if (deep == 3) {
                // 3단계인 경우 -> 부모가 2단계, 조부모가 1단계
                // 3단계 카테고리 자체는 쿠폰 정책이 없으므로 ID 저장 안 함(혹은 필요하면 저장)

                // 1. 부모(2단계) 찾기
                Category parent = category.getPreCategory();
                if (parent != null) {
                    secondCategoryId = parent.getId(); // 2단계 ID 확보

                    // 2. 조부모(1단계) 찾기
                    Category grandParent = parent.getPreCategory();
                    if (firstCategoryId == null && grandParent != null) {
                        firstCategoryId = grandParent.getId(); // 1단계 ID 확보
                    }
                }
            }
        }

        // 로그 확인 (개발 중 디버깅용)
        if (firstCategoryId == null && secondCategoryId == null) {
            log.warn("책 ID {}에 대해 적용 가능한 1, 2단계 카테고리를 찾지 못했습니다.", bookId);
        }

        return new BookCategoryResponse(bookId, firstCategoryId, secondCategoryId);
    }
}
