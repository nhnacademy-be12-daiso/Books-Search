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

    /**
     * 카테고리 등록
     * @param reqDTO
     */
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

    /**
     * 카테고리 수정
     * @param categoryId
     * @param reqDTO
     */
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

    /**
     * 도서에 카테고리 할당
     * @param book
     * @param categoryId
     */
    @Override
    @Transactional
    public void assignCategoriesToBook(Book book, Long categoryId) {
        // 카테고리 ID가 null인 경우 처리
        if(categoryId == null){
            log.error("[도서 등록] category Id가 null입니다 - 해당 도서 ISBN: {}", book.getIsbn());
            return;
        }
        // 카테고리 엔티티 조회
        Category finalCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundCategoryId("[도서 등록] 해당 카테고리를 찾지 못했습니다."));

        // 도서에 카테고리 할당
        BookCategory bc=book.getBookCategory();
        if(bc==null) {  // 처음 할당하는 경우
            bc=new BookCategory(book, finalCategory);
            book.setBookCategory(bc);
        } else {  // 이미 할당된 카테고리가 있는 경우 업데이트
            bc.setCategory(finalCategory);
        }
    }

    /**
     * 여러 도서에 카테고리 할당
     * @param bookMap
     * @param categoryIdMap
     */
    @Override
    @Transactional
    public void assignCategoriesToBooks(Map<String, Book> bookMap, Map<String, Long> categoryIdMap) {
        Set<Long> categoryIdSet = new HashSet<>(categoryIdMap.values());
        Map<Long, Category> categoryMap=categoryRepository.findAllByIdIn(new ArrayList<>(categoryIdSet))
                .stream().collect(Collectors.toMap(Category::getId, c->c));

        List<BookCategory> toSave=new ArrayList<>();

        for(Book book:bookMap.values()) {
            Long categoryId=categoryIdMap.get(book.getIsbn());
            if(categoryId==null) {
                log.error("[여러 도서 등록] category Id가 null입니다 - 해당 도서 ISBN: {}", book.getIsbn());
                continue;
            }

            Category category=categoryMap.get(categoryId);
            if(category==null) {
                log.error("[여러 도서 등록] 해당 카테고리를 찾지 못했습니다 - 해당 도서 ISBN: {} category ID: {}", book.getIsbn(), categoryIdMap.get(book.getIsbn()));
                continue;
            }

            BookCategory bc=book.getBookCategory();
            if(bc==null) {  // 처음 할당하는 경우
                bc=new BookCategory(book, category);
                book.setBookCategory(bc);
            } else {  // 이미 할당된 카테고리가 있는 경우 업데이트
                bc.setCategory(category);
            }
            toSave.add(bc);

        }

        bookCategoryRepository.saveAll(toSave);

    }

    /**
     * 도서 카테고리 수정
     * @param book
     * @param categoryId
     * 단일 카테고리로 변경 하면서 BookCategory 등록 메서드와 동일하게 변경
     */
    @Override
    @Transactional
    public void updateCategoryOfBook(Book book, Long categoryId) {
        // 카테고리 ID가 null인 경우 처리
        if(categoryId == null){
            log.error("[도서 등록] category Id가 null입니다 - 해당 도서 ISBN: {}", book.getIsbn());
            return;
        }
        // 카테고리 엔티티 조회
        Category finalCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundCategoryId("[도서 등록] 해당 카테고리를 찾지 못했습니다."));

        // 도서에 카테고리 할당
        BookCategory bc=book.getBookCategory();
        if(bc==null) {  // 처음 할당하는 경우
            bc=new BookCategory(book, finalCategory);
            book.setBookCategory(bc);
        } else {  // 이미 할당된 카테고리가 있는 경우 업데이트
            bc.setCategory(finalCategory);
        }
    }

    /**
     * 도서 카테고리 삭제
     * @param book
     */
    @Override
    public void deleteCategoryOfBook(Book book) {
        // 단일 카테고리로 변경하면서 BookCategory 등록 메서드와 동일하게 변경
        BookCategory bc=book.getBookCategory();
        if(bc != null){
            bookCategoryRepository.delete(bc);
            book.setBookCategory(null);
        }
    }

    /**
     * 카테고리 리스트 조회
     * @return
     */
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

    /**
     * 전체 카테고리 개수 조회
     * @return
     */
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


    /**
     * 도서 카테고리 조회 (쿠폰 정책용)
     * @param bookId
     * @return
     */
    @Transactional(readOnly = true)
    @Override
    public BookCategoryResponse bookCategory(Long bookId) {

        BookCategory bc = bookCategoryRepository.findByBook_Id(bookId).orElse(null);

        if (bc == null || bc.getCategory() == null) {
            return new BookCategoryResponse(bookId, null, null);
        }

        Category leaf = bc.getCategory();

        Long firstCategoryId = null;
        Long secondCategoryId = null;

        if (leaf.getDeep() == 1) {
            // 1단계인 경우
            firstCategoryId = leaf.getId();
        } else if (leaf.getDeep() == 2) {
            // 2단계인 경우 -> 본인은 2단계, 부모는 1단계
            secondCategoryId = leaf.getId();
            if (leaf.getPreCategory() != null) firstCategoryId = leaf.getPreCategory().getId();
        } else if (leaf.getDeep() == 3) {
            // 3단계인 경우 -> 부모가 2단계, 조부모가 1단계
            // 3단계 카테고리 자체는 쿠폰 정책이 없으므로 ID 저장 안 함(혹은 필요하면 저장)

            // 1. 부모(2단계) 찾기
            Category parent = leaf.getPreCategory();
            if (parent != null) {
                secondCategoryId = parent.getId();

                // 2. 조부모(1단계) 찾기
                Category grand = parent.getPreCategory();
                if (grand != null) firstCategoryId = grand.getId(); // 1단계 ID 확보
            }
        }

        // 로그 확인 (개발 중 디버깅용)
        if (firstCategoryId == null && secondCategoryId == null) {
            log.warn("책 ID {}에 대해 적용 가능한 1, 2단계 카테고리를 찾지 못했습니다.", bookId);
        }

        return new BookCategoryResponse(bookId, firstCategoryId, secondCategoryId);
    }

}
