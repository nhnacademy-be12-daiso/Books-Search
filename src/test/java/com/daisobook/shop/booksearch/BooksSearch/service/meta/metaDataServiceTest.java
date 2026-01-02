package com.daisobook.shop.booksearch.BooksSearch.service.meta;

import com.daisobook.shop.booksearch.BooksSearch.dto.api.BookInfoDataView;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.RoleNameListRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.TotalDataRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.book.BookAdminResponseDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.book.BookUpdateView;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.category.CategoryList;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.meta.AdminBookMetaData;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.meta.FindIsbnMetaData;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.meta.ModifyBookMetaData;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.meta.RegisterBookMetaData;
import com.daisobook.shop.booksearch.BooksSearch.service.api.BookRefineService;
import com.daisobook.shop.booksearch.BooksSearch.service.author.AuthorV2Service;
import com.daisobook.shop.booksearch.BooksSearch.service.book.impl.BookFacade;
import com.daisobook.shop.booksearch.BooksSearch.service.category.CategoryV2Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class metaDataServiceTest {

    @MockitoBean
    @Mock
    private BookRefineService bookRefineService;

    @MockitoBean @Mock
    private CategoryV2Service categoryService;

    @MockitoBean @Mock
    private AuthorV2Service authorService;

    @MockitoBean @Mock
    private BookFacade bookFacade;

    @InjectMocks
    private MetadataService metadataService;

    @BeforeEach
    void setUp() {
        // 각 테스트에서 필요한 스텁을 명확히 설정하도록 비워둠
    }

    @Test
    @DisplayName("getAdminBookMataData 성공 시, BookFacade의 메서드들이 호출되고 AdminBookMetaData 반환")
    void getAdminBookMataData_success_invokesFacadeAndReturnsMeta() {
        Pageable pageable = PageRequest.of(0, 10);
        @SuppressWarnings("unchecked")
        Page<BookAdminResponseDTO> pageMock = mock(Page.class);
        TotalDataRespDTO totalMock = mock(TotalDataRespDTO.class);

        when(bookFacade.findAllForAdmin(pageable)).thenReturn(pageMock);
        when(bookFacade.getTotalDate()).thenReturn(totalMock);

        AdminBookMetaData result = metadataService.getAdminBookMataData(pageable);

        assertNotNull(result, "AdminBookMetaData가 null이면 실패");
        // 핵심 호출들이 수행됐는지 확인 (문제가 난 위치를 바로 알 수 있음)
        verify(bookFacade, times(1)).findAllForAdmin(pageable);
        verify(bookFacade, times(1)).getTotalDate();
    }

    @Test
    @DisplayName("getAdminBookMataData 실패 시, BookFacade의 예외가 전파됨")
    void getAdminBookMataData_whenFacadeThrows_propagatesException() {
        Pageable pageable = PageRequest.of(0, 10);
        when(bookFacade.findAllForAdmin(pageable)).thenThrow(new RuntimeException("findAll-fail"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> metadataService.getAdminBookMataData(pageable));
        assertEquals("findAll-fail", ex.getMessage());
        verify(bookFacade, times(1)).findAllForAdmin(pageable);
        // getTotalDate는 호출되지 않아야 함
        verify(bookFacade, never()).getTotalDate();
    }

    @Test
    @DisplayName("getRegisterBookMataData 성공 시, 카테고리와 역할 목록을 반환")
    void getRegisterBookMataDataFromAdmin_success_returnsCategoryAndRoles() {
        CategoryList catMock = mock(CategoryList.class);
        RoleNameListRespDTO roleMock = mock(RoleNameListRespDTO.class);

        when(categoryService.getCategoryList()).thenReturn(catMock);
        when(authorService.getRoleNameList()).thenReturn(roleMock);

        RegisterBookMetaData result = metadataService.getRegisterBookMataDataFromAdmin();

        assertNotNull(result, "RegisterBookMetaData가 null이면 실패");
        verify(categoryService, times(1)).getCategoryList();
        verify(authorService, times(1)).getRoleNameList();
    }

    @Test
    @DisplayName("getModifyBookMataData 성공 시, 도서 수정 뷰, 카테고리, 역할 목록을 반환")
    void getModifyBookMataDataFromAdmin_success_returnsMergedData() {
        long bookId = 42L;
        BookUpdateView updateMock = mock(BookUpdateView.class);
        CategoryList catMock = mock(CategoryList.class);
        RoleNameListRespDTO roleMock = mock(RoleNameListRespDTO.class);

        when(bookFacade.getBookUpdateView(bookId)).thenReturn(updateMock);
        when(categoryService.getCategoryList()).thenReturn(catMock);
        when(authorService.getRoleNameList()).thenReturn(roleMock);

        ModifyBookMetaData result = metadataService.getModifyBookMataDataFromAdmin(bookId);

        assertNotNull(result, "ModifyBookMetaData가 null이면 실패");
        verify(bookFacade, times(1)).getBookUpdateView(bookId);
        verify(categoryService, times(1)).getCategoryList();
        verify(authorService, times(1)).getRoleNameList();
    }

    @Test
    @DisplayName("getFindIsbnMataData 성공 시, 정제된 도서 정보, 카테고리, 역할 목록을 반환")
    void getFindIsbnMataDataFromAdmin_success_returnsRefinedAndMeta() {
        String isbn = "978-1-23456-789-7";
        BookInfoDataView refinedMock = mock(BookInfoDataView.class);
        CategoryList catMock = mock(CategoryList.class);
        RoleNameListRespDTO roleMock = mock(RoleNameListRespDTO.class);

        when(bookRefineService.getRefinedBook(isbn)).thenReturn(refinedMock);
        when(categoryService.getCategoryList()).thenReturn(catMock);
        when(authorService.getRoleNameList()).thenReturn(roleMock);

        FindIsbnMetaData result = metadataService.getFindIsbnMataDataFromAdmin(isbn);

        assertNotNull(result, "FindIsbnMetaData가 null이면 실패");
        verify(bookRefineService, times(1)).getRefinedBook(isbn);
        verify(categoryService, times(1)).getCategoryList();
        verify(authorService, times(1)).getRoleNameList();
    }

    @Test
    @DisplayName("getFindIsbnMataData 실패 시, BookRefineService의 예외가 전파됨")
    void getFindIsbnMataDataFromAdmin_whenRefineThrows_propagatesException() {
        String isbn = "bad-isbn";
        when(bookRefineService.getRefinedBook(isbn)).thenThrow(new RuntimeException("refine-fail"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> metadataService.getFindIsbnMataDataFromAdmin(isbn));
        assertEquals("refine-fail", ex.getMessage());
        verify(bookRefineService, times(1)).getRefinedBook(isbn);
        // 이후 의존 서비스는 호출되지 않아야 함
        verify(categoryService, never()).getCategoryList();
        verify(authorService, never()).getRoleNameList();
    }
}
