package com.daisobook.shop.booksearch.BooksSearch.service.category;

import com.daisobook.shop.booksearch.BooksSearch.dto.projection.CategoryListProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.category.CategoryRegisterReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.category.CategoryModifyReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.CategoryPathProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.category.CategoryList;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.category.CategoryTree;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.category.CategoryTreeListRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.coupon.CategorySimpleResponse;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.entity.category.BookCategory;
import com.daisobook.shop.booksearch.BooksSearch.entity.category.Category;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.category.DuplicatedCategory;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.category.ExistedCategory;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.category.InvalidCategoryDepthException;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.category.NotFoundCategoryId;
import com.daisobook.shop.booksearch.BooksSearch.mapper.category.CategoryMapper;
import com.daisobook.shop.booksearch.BooksSearch.repository.category.BookCategoryRepository;
import com.daisobook.shop.booksearch.BooksSearch.repository.category.CategoryRepository;
import com.daisobook.shop.booksearch.BooksSearch.service.category.impl.CategoryV2ServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    BookCategoryRepository bookCategoryRepository;

    @Mock
    CategoryRepository categoryRepository;

    @Mock
    CategoryMapper categoryMapper;

    @InjectMocks
    CategoryV2ServiceImpl categoryService;

    @Test
    @DisplayName("registerCategory: 성공 - 최상위(깊이 1) 카테고리 등록")
    void registerCategory_Success_TopLevel() {
        CategoryRegisterReqDTO req = mock(CategoryRegisterReqDTO.class);
        when(req.categoryId()).thenReturn(100L);
        when(req.name()).thenReturn("테스트카테고리");
        when(req.deep()).thenReturn(1);
        when(req.preCategoryId()).thenReturn(null);

        when(categoryRepository.existsCategoryById(100L)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        categoryService.registerCategory(req);

        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("registerCategory: 중복 ID 등록시 ExistedCategory 발생")
    void registerCategory_DuplicateId_ThrowsExistedCategory() {
        CategoryRegisterReqDTO req = mock(CategoryRegisterReqDTO.class);
        when(req.categoryId()).thenReturn(200L);
        when(categoryRepository.existsCategoryById(200L)).thenReturn(true);

        ExistedCategory ex = assertThrows(ExistedCategory.class, () -> categoryService.registerCategory(req));
        assertNotNull(ex.getMessage());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerCategory: 상위 카테고리 없음 -> NotFoundCategoryId 발생")
    void registerCategory_PreCategoryNotFound_ThrowsNotFoundCategoryId() {
        CategoryRegisterReqDTO req = mock(CategoryRegisterReqDTO.class);
        when(req.categoryId()).thenReturn(300L);
        when(req.name()).thenReturn("하위");
        when(req.deep()).thenReturn(2);
        when(req.preCategoryId()).thenReturn(999L);

        when(categoryRepository.existsCategoryById(300L)).thenReturn(false);
        when(categoryRepository.findCategoryById(999L)).thenReturn(null);

        assertThrows(NotFoundCategoryId.class, () -> categoryService.registerCategory(req));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("assignCategoriesToBook: categoryId null이면 아무 동작 안함")
    void assignCategoriesToBook_NullCategoryId_NoInteraction() {
        Book book = mock(Book.class);
        when(book.getIsbn()).thenReturn("ISBN-NULL");

        categoryService.assignCategoriesToBook(book, null);

        verifyNoInteractions(categoryRepository);
        verifyNoInteractions(bookCategoryRepository);
    }

    @Test
    @DisplayName("assignCategoriesToBook: 카테고리 경로 못 찾으면 NotFoundCategoryId 발생")
    void assignCategoriesToBook_PathNotFound_ThrowsNotFound() {
        Book book = mock(Book.class);
        // book.getIsbn() 스텁은 불필요하므로 제거

        when(categoryRepository.findAncestorsPathByFinalCategoryId(404L)).thenReturn(Collections.emptyList());

        assertThrows(NotFoundCategoryId.class, () -> categoryService.assignCategoriesToBook(book, 404L));
        verify(bookCategoryRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("assignCategoriesToBook: 정상 등록 시 BookCategory 저장 호출")
    void assignCategoriesToBook_Success_SavesBookCategories() {
        Book book = mock(Book.class);
        // book.getIsbn() 스텁 제거(사용되지 않아 불필요)
        when(book.getId()).thenReturn(1L);
        // 리턴되는 bookCategories 리스트는 실제 리스트로 반환되게 설정
        List<BookCategory> bookCategoryList = new ArrayList<>();
        when(book.getBookCategories()).thenReturn(bookCategoryList);

        // projections: 최종 카테고리(3) -> 조상들 3,2,1 반환 시나리오
        CategoryPathProjection p1 = mock(CategoryPathProjection.class);
        when(p1.getId()).thenReturn(3L);
        CategoryPathProjection p2 = mock(CategoryPathProjection.class);
        when(p2.getId()).thenReturn(2L);
        CategoryPathProjection p3 = mock(CategoryPathProjection.class);
        when(p3.getId()).thenReturn(1L);

        when(categoryRepository.findAncestorsPathByFinalCategoryId(3L)).thenReturn(Arrays.asList(p1, p2, p3));

        // findAllByIdIn -> 실제 Category 객체 대신 mock으로 최소 동작 보장
        Category cat1 = mock(Category.class);
        when(cat1.getId()).thenReturn(1L);
        when(cat1.getBookCategories()).thenReturn(new ArrayList<>());

        Category cat2 = mock(Category.class);
        when(cat2.getId()).thenReturn(2L);
        when(cat2.getBookCategories()).thenReturn(new ArrayList<>());

        Category cat3 = mock(Category.class);
        when(cat3.getId()).thenReturn(3L);
        when(cat3.getBookCategories()).thenReturn(new ArrayList<>());

        when(categoryRepository.findAllByIdIn(anyList())).thenReturn(Arrays.asList(cat3, cat2, cat1));

        // 실행
        categoryService.assignCategoriesToBook(book, 3L);

        // saveAll 호출 확인 (3개 카테고리 링크)
        verify(bookCategoryRepository, times(1)).saveAll(anyList());
        // 각 카테고리와 책의 리스트가 업데이트 되었는지도 부분 검증
        assertEquals(3, book.getBookCategories().size());
    }

    @Test
    @DisplayName("bookCategory: 깊이별 조상 추출 - 1단계 카테고리만 존재")
    void bookCategory_OnlyFirstLevel() {
        long bookId = 10L;
        BookCategory bc = mock(BookCategory.class);
        Category c1 = mock(Category.class);
        when(c1.getDeep()).thenReturn(1);
        when(c1.getId()).thenReturn(11L);
        when(bc.getCategory()).thenReturn(c1);

        when(bookCategoryRepository.findAllByBook_Id(bookId)).thenReturn(Arrays.asList(bc));

        var resp = categoryService.bookCategory(bookId);

        assertEquals(bookId, resp.getBookId());
        assertEquals(11L, resp.getPrimaryCategoryId());
        assertNull(resp.getSecondaryCategoryId());
    }

    @Test
    @DisplayName("bookCategory: 3단계 카테고리 존재 시 부모와 조부모 추출")
    void bookCategory_ThirdLevel_ExtractsParentAndGrandparent() {
        long bookId = 20L;

        // 3단계 카테고리
        Category grandParent = mock(Category.class);
        when(grandParent.getId()).thenReturn(100L);
        // when(grandParent.getDeep()).thenReturn(1);  // 제거: 불필요한 스텁

        Category parent = mock(Category.class);
        when(parent.getId()).thenReturn(200L);
        // when(parent.getDeep()).thenReturn(2); // 제거: 불필요한 스텁
        when(parent.getPreCategory()).thenReturn(grandParent);

        Category child = mock(Category.class);
        when(child.getDeep()).thenReturn(3);
        when(child.getPreCategory()).thenReturn(parent);

        BookCategory bc = mock(BookCategory.class);
        when(bc.getCategory()).thenReturn(child);

        when(bookCategoryRepository.findAllByBook_Id(bookId)).thenReturn(Arrays.asList(bc));

        var resp = categoryService.bookCategory(bookId);

        assertEquals(bookId, resp.getBookId());
        assertEquals(100L, resp.getPrimaryCategoryId());
        assertEquals(200L, resp.getSecondaryCategoryId());
    }

    @Test
    @DisplayName("deleteCategory: 하위 카테고리 존재하면 DuplicatedCategory 발생")
    void deleteCategory_WhenHasChild_ThrowsDuplicatedCategory() {
        long categoryId = 555L;
        when(categoryRepository.existsCategoryById(categoryId)).thenReturn(true);
        when(categoryRepository.existsCategoriesByPreCategory_Id(categoryId)).thenReturn(true);

        assertThrows(DuplicatedCategory.class, () -> categoryService.deleteCategory(categoryId));
        verify(categoryRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("deleteCategory: 연결된 BookCategory가 있을 경우 임시 카테고리로 재배치 후 삭제 수행")
    void deleteCategory_WhenLinkedBooks_ReassignToTempAndDelete() {
        long categoryId = 66L;
        when(categoryRepository.existsCategoryById(categoryId)).thenReturn(true);
        when(categoryRepository.existsCategoriesByPreCategory_Id(categoryId)).thenReturn(false);

        BookCategory link = mock(BookCategory.class);
        Book book = mock(Book.class);

        List<BookCategory> bookLinkList = new ArrayList<>();
        bookLinkList.add(link);
        when(book.getBookCategories()).thenReturn(bookLinkList);

        when(link.getBook()).thenReturn(book);
        // when(link.getCategory()).thenReturn(null); // 제거: 사용되지 않음

        List<BookCategory> targetLinks = Arrays.asList(link);
        when(bookCategoryRepository.findAllByCategoryIdWithBook(categoryId)).thenReturn(targetLinks);

        Category tempCategory = mock(Category.class);
        when(categoryRepository.findCategoryByName("임시 카테고리")).thenReturn(tempCategory);

        // doNothing() 으로 스텁할 필요 없음 - 기본 동작은 아무 것도 하지 않음

        categoryService.deleteCategory(categoryId);

        verify(bookCategoryRepository, times(1)).findAllByCategoryIdWithBook(categoryId);
        verify(bookCategoryRepository, times(1)).deleteAllInBatch(targetLinks);
        verify(categoryRepository, times(1)).deleteById(categoryId);
    }

    @Test
    @DisplayName("modifyCategory: 존재하지 않으면 ExistedCategory 발생")
    void modifyCategory_NotExist_ThrowsExistedCategory() {
        long id = 1000L;
        when(categoryRepository.existsCategoryById(id)).thenReturn(false);

        CategoryModifyReqDTO req = mock(CategoryModifyReqDTO.class);
        ExistedCategory ex = assertThrows(ExistedCategory.class, () -> categoryService.modifyCategory(id, req));
        assertNotNull(ex.getMessage());
        verify(categoryRepository, never()).findCategoryById(anyLong());
    }

    @Test
    @DisplayName("modifyCategory: 상위 카테고리 단계가 맞지 않으면 InvalidCategoryDepthException 발생")
    void modifyCategory_InvalidDepth_ThrowsInvalidCategoryDepthException() {
        long id = 1001L;
        when(categoryRepository.existsCategoryById(id)).thenReturn(true);

        Category category = mock(Category.class);
        Category pre = mock(Category.class);
        when(categoryRepository.findCategoryById(id)).thenReturn(category);
        when(category.getPreCategory()).thenReturn(pre);
        when(pre.getDeep()).thenReturn(3);
        when(category.getPreCategory()).thenReturn(pre);
        when(category.getDeep()).thenReturn(4);

        CategoryModifyReqDTO req = mock(CategoryModifyReqDTO.class);
        when(req.deep()).thenReturn(2); // pre.getDeep() (3) >= req.deep() (2) -> 예외

        assertThrows(InvalidCategoryDepthException.class, () -> categoryService.modifyCategory(id, req));
    }

    @Test
    @DisplayName("modifyCategory: 이름과 단계 변경 성공")
    void modifyCategory_Success_ChangeNameAndDeep() {
        long id = 1002L;
        when(categoryRepository.existsCategoryById(id)).thenReturn(true);

        Category category = mock(Category.class);
        when(categoryRepository.findCategoryById(id)).thenReturn(category);
        when(category.getName()).thenReturn("oldName");
        when(category.getDeep()).thenReturn(1);
        when(category.getPreCategory()).thenReturn(null);

        CategoryModifyReqDTO req = mock(CategoryModifyReqDTO.class);
        when(req.name()).thenReturn("newName");
        when(req.deep()).thenReturn(2);

        categoryService.modifyCategory(id, req);

        verify(category).setName("newName");
        verify(category).setDeep(2);
    }

    // java
    @Test
    @DisplayName("assignCategoriesToBooks: 여러 도서에 대해 카테고리 연결 저장")
    void assignCategoriesToBooks_Success() {
        Book book = mock(Book.class);
        when(book.getIsbn()).thenReturn("ISBN-A");
        List<BookCategory> bookCatList = new ArrayList<>();
        when(book.getBookCategories()).thenReturn(bookCatList);

        Map<String, Book> bookMap = new HashMap<>();
        bookMap.put("k1", book);

        Map<String, Long> categoryIdMap = new HashMap<>();
        categoryIdMap.put("ISBN-A", 3L);

        // projections for 3 -> 2 -> 1
        CategoryPathProjection p3 = mock(CategoryPathProjection.class);
        when(p3.getId()).thenReturn(3L);
        CategoryPathProjection p2 = mock(CategoryPathProjection.class);
        when(p2.getId()).thenReturn(2L);
        CategoryPathProjection p1 = mock(CategoryPathProjection.class);
        when(p1.getId()).thenReturn(1L);

        // preCategoryId는 while 루프에서 사용되므로 필요한 경우만 lenient로 허용
        lenient().when(p3.getPreCategoryId()).thenReturn(2L);
        lenient().when(p2.getPreCategoryId()).thenReturn(1L);
        lenient().when(p1.getPreCategoryId()).thenReturn(0L);

        when(categoryRepository.findAncestorsPathByFinalCategoryIdIn(anyList()))
                .thenReturn(Arrays.asList(p3, p2, p1));

        Category c1 = mock(Category.class);
        when(c1.getId()).thenReturn(1L);
        when(c1.getBookCategories()).thenReturn(new ArrayList<>());

        Category c2 = mock(Category.class);
        when(c2.getId()).thenReturn(2L);
        when(c2.getBookCategories()).thenReturn(new ArrayList<>());

        Category c3 = mock(Category.class);
        when(c3.getId()).thenReturn(3L);
        when(c3.getBookCategories()).thenReturn(new ArrayList<>());

        when(categoryRepository.findAllByIdIn(anyList())).thenReturn(Arrays.asList(c3, c2, c1));

        categoryService.assignCategoriesToBooks(bookMap, categoryIdMap);

        verify(bookCategoryRepository, times(1)).saveAll(anyList());
        // 조상 체인(3,2,1) 만큼 BookCategory가 추가되어야 함
        assertEquals(3, book.getBookCategories().size());
    }


    // java
    @Test
    @DisplayName("updateCategoryOfBook: 새로운 레벨 추가시 saveAll 호출")
    void updateCategoryOfBook_AddNewLevel_SaveAllCalled() {
        Book book = mock(Book.class);
        List<BookCategory> preList = new ArrayList<>();
        BookCategory bc1 = mock(BookCategory.class);
        Category preCat = mock(Category.class);
        // preCat의 부모(preCategory)를 mock으로 설정하여 서비스 로그에서 NPE 방지
        Category preCatParent = mock(Category.class);
        when(preCat.getPreCategory()).thenReturn(preCatParent);

        when(preCat.getDeep()).thenReturn(1);
        when(preCat.getId()).thenReturn(11L);
        when(bc1.getCategory()).thenReturn(preCat);
        // when(bc1.getId()).thenReturn(501L); // 제거: 사용되지 않음
        preList.add(bc1);
        when(book.getBookCategories()).thenReturn(preList);
        // when(book.getIsbn()).thenReturn("ISBN-UP"); // 제거: updateCategoryOfBook에서 사용되지 않음

        // projections -> two ids (100 deep1, 200 deep2)
        CategoryPathProjection p100 = mock(CategoryPathProjection.class);
        when(p100.getId()).thenReturn(100L);
        CategoryPathProjection p200 = mock(CategoryPathProjection.class);
        when(p200.getId()).thenReturn(200L);

        when(categoryRepository.findAncestorsPathByFinalCategoryId(999L)).thenReturn(Arrays.asList(p100, p200));

        Category cat100 = mock(Category.class);
        when(cat100.getDeep()).thenReturn(1);
        when(cat100.getId()).thenReturn(100L);
        when(cat100.getBookCategories()).thenReturn(new ArrayList<>());
        // 부모가 null이 아니도록만 설정 (getName() 스텁은 제거하여 불필요한 스텁 방지)
        Category cat100Parent = mock(Category.class);
        when(cat100.getPreCategory()).thenReturn(cat100Parent);

        Category cat200 = mock(Category.class);
        when(cat200.getDeep()).thenReturn(2);
        when(cat200.getId()).thenReturn(200L);
        when(cat200.getBookCategories()).thenReturn(new ArrayList<>());
        when(cat200.getPreCategory()).thenReturn(cat100);

        when(categoryRepository.findAllByIdIn(anyList())).thenReturn(Arrays.asList(cat100, cat200));

        // call
        categoryService.updateCategoryOfBook(book, 999L);

        verify(bookCategoryRepository, times(1)).saveAll(anyList());
        // 하나의 새 BookCategory가 추가되어야 함
        assertTrue(book.getBookCategories().size() >= 1);
    }







    @Test
    @DisplayName("deleteCategoryOfBook: 연결된 BookCategory 삭제 및 연관 리스트 제거")
    void deleteCategoryOfBook_Success() {
        Book book = mock(Book.class);
        Category catA = mock(Category.class);
        Category catB = mock(Category.class);

        List<BookCategory> bookCategories = new ArrayList<>();
        BookCategory bcA = mock(BookCategory.class);
        BookCategory bcB = mock(BookCategory.class);
        when(bcA.getCategory()).thenReturn(catA);
        when(bcB.getCategory()).thenReturn(catB);
        bookCategories.add(bcA);
        bookCategories.add(bcB);

        List<BookCategory> catABookList = new ArrayList<>(bookCategories);
        when(catA.getBookCategories()).thenReturn(new ArrayList<>(catABookList));
        when(catB.getBookCategories()).thenReturn(new ArrayList<>(catABookList));

        when(book.getBookCategories()).thenReturn(new ArrayList<>(bookCategories));

        categoryService.deleteCategoryOfBook(book);

        verify(bookCategoryRepository, times(1)).deleteAll(anyList());
        assertTrue(book.getBookCategories().isEmpty());
    }

    @Test
    @DisplayName("getCategoryList: 매퍼 호출 및 결과 전달")
    void getCategoryList_ReturnsMappedResult() {
        CategoryListProjection p1 = mock(CategoryListProjection.class);
        when(p1.getCategoryId()).thenReturn(1L);
        when(p1.getPreCategoryId()).thenReturn(null);

        CategoryListProjection p2 = mock(CategoryListProjection.class);
        when(p2.getCategoryId()).thenReturn(2L);
        when(p2.getPreCategoryId()).thenReturn(1L);

        when(categoryRepository.getAll()).thenReturn(Arrays.asList(p1, p2));

        CategoryList mapped = mock(CategoryList.class);
        when(categoryMapper.toCategoryList(anyMap(), anyList())).thenReturn(mapped);

        CategoryList result = categoryService.getCategoryList();
        assertSame(mapped, result);
        verify(categoryMapper, times(1)).toCategoryList(anyMap(), anyList());
    }

    @Test
    @DisplayName("getCountAll: repository count 반환값 전달")
    void getCountAll_ReturnsCount() {
        when(categoryRepository.count()).thenReturn(42L);
        Long cnt = categoryService.getCountAll();
        assertEquals(42L, cnt);
    }

    @Test
    @DisplayName("getCategoryTreeList: 트리 매퍼 결과 래핑")
    void getCategoryTreeList_ReturnsTreeDto() {
        CategoryListProjection p = mock(CategoryListProjection.class);
        when(categoryRepository.getAll()).thenReturn(Arrays.asList(p));

        CategoryTree tree = mock(CategoryTree.class);
        when(categoryMapper.toCategoryTreeList(anyList())).thenReturn(Arrays.asList(tree));

        CategoryTreeListRespDTO resp = categoryService.getCategoryTreeList();
        assertNotNull(resp);
        assertEquals(1, resp.categoryTreeList().size());
    }

    @Test
    @DisplayName("findByIdIn: 카테고리 목록을 간단 응답으로 변환")
    void findByIdIn_ReturnsSimpleResponses() {
        Category c1 = mock(Category.class);
        when(c1.getId()).thenReturn(11L);
        when(c1.getName()).thenReturn("C1");
        Category c2 = mock(Category.class);
        when(c2.getId()).thenReturn(22L);
        when(c2.getName()).thenReturn("C2");

        when(categoryRepository.findByIdIn(Arrays.asList(11L, 22L))).thenReturn(Arrays.asList(c1, c2));

        List<CategorySimpleResponse> resp = categoryService.findByIdIn(Arrays.asList(11L, 22L));
        assertEquals(2, resp.size());
        assertEquals(11L, resp.get(0).categoryId());
        assertEquals("C1", resp.get(0).categoryName());
    }

}
