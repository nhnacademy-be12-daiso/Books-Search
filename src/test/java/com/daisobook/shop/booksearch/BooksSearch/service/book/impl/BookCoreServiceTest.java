package com.daisobook.shop.booksearch.BooksSearch.service.book.impl;

import com.daisobook.shop.booksearch.BooksSearch.dto.BookUpdateData;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.BookIdProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.AuthorReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Status;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.book.DuplicatedBook;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.book.NotFoundBookISBN;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.book.NotFoundBookId;
import com.daisobook.shop.booksearch.BooksSearch.repository.BookOfTheWeekRepository;
import com.daisobook.shop.booksearch.BooksSearch.repository.book.BookRepository;
import com.daisobook.shop.booksearch.BooksSearch.search.component.BookSearchSyncPublisher;
import com.daisobook.shop.booksearch.BooksSearch.service.author.AuthorV2Service;
import com.daisobook.shop.booksearch.BooksSearch.service.category.CategoryV2Service;
import com.daisobook.shop.booksearch.BooksSearch.service.image.impl.BookImageServiceImpl;
import com.daisobook.shop.booksearch.BooksSearch.service.like.LikeService;
import com.daisobook.shop.booksearch.BooksSearch.service.publisher.PublisherV2Service;
import com.daisobook.shop.booksearch.BooksSearch.service.review.ReviewService;
import com.daisobook.shop.booksearch.BooksSearch.service.tag.TagV2Service;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookCoreServiceTest {

    @Mock
    BookRepository bookRepository;
    @Mock
    LikeService likeService;
    @Mock
    ReviewService reviewService;
    @Mock
    CategoryV2Service categoryService;
    @Mock
    TagV2Service tagService;
    @Mock
    AuthorV2Service authorService;
    @Mock
    PublisherV2Service publisherService;
    @Mock
    BookImageServiceImpl imageService;
    @Mock
    BookOfTheWeekRepository bookOfTheWeekRepository;
    @Mock
    BookSearchSyncPublisher bookSearchSyncPublisher;

    @InjectMocks BookCoreService sut;

    @Test
    @DisplayName("validateExistsById: 존재하지 않으면 NotFoundBookId")
    void validateExistsById_notFound_throws() {
        when(bookRepository.existsBookById(1L)).thenReturn(false);

        assertThatThrownBy(() -> sut.validateExistsById(1L))
                .isInstanceOf(NotFoundBookId.class);
    }

    @Test
    @DisplayName("validateNotExistsByIsbn: 이미 존재하면 DuplicatedBook")
    void validateNotExistsByIsbn_duplicated_throws() {
        when(bookRepository.existsBookByIsbn("X")).thenReturn(true);

        assertThatThrownBy(() -> sut.validateNotExistsByIsbn("X"))
                .isInstanceOf(DuplicatedBook.class);
    }

    @Test
    @DisplayName("registerBook: 저장 및 관련 서비스 호출")
    void registerBook_success() {
        Book book = new Book();
        book.setIsbn("I-1");

        book.setBookAuthors(List.of());
        when(bookRepository.save(book)).thenReturn(book);

        Book result = sut.registerBook(book, 10L, List.of("t1"), List.of(), "P");

        assertThat(result).isSameAs(book);
        verify(bookRepository).save(book);
        verify(categoryService).assignCategoriesToBook(book, 10L);
        verify(tagService).assignTagsToBook(book, List.of("t1"));
        verify(authorService).assignAuthorsToBook(eq(book), anyList());
        verify(publisherService).assignPublisherToBook(book, "P");
    }

    @Test
    @DisplayName("getExistsByIsbn: 빈 결과면 null 반환, 값 있으면 Set 반환")
    void getExistsByIsbn_behavior() {
        when(bookRepository.findBooksByIsbnIn(List.of("A"))).thenReturn(List.of());
        assertThat(sut.getExistsByIsbn(List.of("A"))).isNull();

        BookIdProjection p = mock(BookIdProjection.class);
        // lenient로 불필요한 스텁 경고 회피 + 제네릭 추론 회피
        lenient().doReturn(Collections.singletonList(p))
                .when(bookRepository).findBooksByIsbnIn(List.of("B"));

        var set = sut.getExistsByIsbn(List.of("B"));
        assertThat(set).isNotNull();
    }


    @Test
    @DisplayName("getBook_Id: 없으면 null, 있으면 반환")
    void getBook_Id_behavior() {
        when(bookRepository.findBookById(2L)).thenReturn(null);
        assertThat(sut.getBook_Id(2L)).isNull();

        Book book = new Book();
        when(bookRepository.findBookById(3L)).thenReturn(book);
        assertThat(sut.getBook_Id(3L)).isSameAs(book);
    }

    @Test
    @DisplayName("updateBookByData: 필드 변경 적용 및 관련 서비스 호출, 삭제 시 publish")
    void updateBookByData_updates_and_publishOnDelete() {
        Book book = new Book();
        book.setIsbn("S-100");
        book.setTitle("old");
        book.setIndex(String.valueOf(1L));
        book.setDescription("d");
        book.setPublicationDate(LocalDate.of(2000,1,1));
        book.setPrice(1000L);
        book.setStock(1);
        book.setStatus(Status.ON_SALE);
        book.setVolumeNo(1);
        book.setDeleted(false);

        BookUpdateData dto = mock(BookUpdateData.class);
        when(dto.title()).thenReturn("new");
        when(dto.index()).thenReturn(String.valueOf(2L));
        when(dto.description()).thenReturn("nd");
        when(dto.price()).thenReturn(2000L);
        when(dto.isPackaging()).thenReturn(true);
        when(dto.stock()).thenReturn(5);
        when(dto.status()).thenReturn(Status.ON_SALE);
        when(dto.volumeNo()).thenReturn(10);
        when(dto.isDeleted()).thenReturn(true);
        when(dto.author()).thenReturn(null);
        when(dto.category()).thenReturn(null);
        when(dto.tag()).thenReturn(null);
        when(dto.publisher()).thenReturn(null);

        Book updated = sut.updateBookByData(book, dto);

        assertThat(updated.getTitle()).isEqualTo("new");
        assertThat(updated.getIndex()).isEqualTo(String.valueOf(2L));
        assertThat(updated.getDescription()).isEqualTo("nd");
        assertThat(updated.getPublicationDate()).isEqualTo(LocalDate.of(2000,1,1));
        assertThat(updated.getPrice()).isEqualTo(2000L);
        assertThat(updated.isPackaging()).isTrue();
        assertThat(updated.getStock()).isEqualTo(5);
        assertThat(updated.getStatus()).isEqualTo(Status.ON_SALE);
        assertThat(updated.getVolumeNo()).isEqualTo(10);
        assertThat(updated.isDeleted()).isTrue();

        verify(authorService).updateAuthorOfBook(eq(book), any());
        verify(categoryService).updateCategoryOfBook(eq(book), any());
        verify(tagService).updateTagOfBook(eq(book), any());
        verify(publisherService).updatePublisherOfBook(eq(book), any());
        verify(bookSearchSyncPublisher).publishDeleteAfterCommit("S-100", "BOOK_DELETE");
    }

    @Test
    @DisplayName("deleteBookByData: 관련 삭제 위임")
    void deleteBookByData_delegates() {
        Book book = new Book();
        sut.deleteBookByData(book);

        verify(authorService).deleteAuthorOfBook(book);
        verify(categoryService).deleteCategoryOfBook(book);
        verify(tagService).deleteTagOfBook(book);
        verify(publisherService).deletePublisherOfBook(book);
    }

    @Test
    @DisplayName("getBookIdByIsbn: null/blank 또는 미발견 시 예외, 있으면 id 반환")
    void getBookIdByIsbn_behaviors() {
        assertThatThrownBy(() -> sut.getBookIdByIsbn(null))
                .isInstanceOf(NotFoundBookISBN.class);

        BookIdProjection missing = null;
        when(bookRepository.getBookId("X")).thenReturn(null);
        assertThatThrownBy(() -> sut.getBookIdByIsbn("X"))
                .isInstanceOf(NotFoundBookISBN.class);

        BookIdProjection p = mock(BookIdProjection.class);
        when(p.getId()).thenReturn(42L);
        when(bookRepository.getBookId("S-1")).thenReturn(p);
        assertThat(sut.getBookIdByIsbn("S-1")).isEqualTo(42L);
    }

    @Test
    @DisplayName("getBookIdsOfNewReleases: projection -> id 매핑")
    void getBookIdsOfNewReleases_mapsIds() {
        BookIdProjection p = mock(BookIdProjection.class);
        when(p.getId()).thenReturn(7L);
        when(bookRepository.getBookIdByNewReleases(any(), any())).thenReturn(List.of(p));

        var ids = sut.getBookIdsOfNewReleases(null, null, 5);
        assertThat(ids).containsExactly(7L);
    }

    @Test
    @DisplayName("getBookIdsFromBookOfTheWeek: projection -> id 매핑")
    void getBookIdsFromBookOfTheWeek_mapsIds() {
        BookIdProjection p = mock(BookIdProjection.class);
        when(p.getId()).thenReturn(9L);
        when(bookOfTheWeekRepository.getBookId(any())).thenReturn(List.of(p));

        var ids = sut.getBookIdsFromBookOfTheWeek(null,3);
        assertThat(ids).containsExactly(9L);
    }

    @Test
    @DisplayName("count 관련 위임")
    void counts_delegate() {
        when(bookRepository.count()).thenReturn(10L);
        when(bookRepository.countAllByStatus(Status.ON_SALE)).thenReturn(3L);

        assertThat(sut.getCountAll()).isEqualTo(10L);
        assertThat(sut.getCountByStatus(Status.ON_SALE)).isEqualTo(3L);
    }

    @Test
    @DisplayName("validateExistsByIsbn: ISBN이 존재하지 않으면 NotFoundBookISBN 발생")
    void validateExistsByIsbn_notFound_throws() {
        when(bookRepository.existsBookByIsbn("12345")).thenReturn(false);

        assertThatThrownBy(() -> sut.validateExistsByIsbn("12345"))
                .isInstanceOf(NotFoundBookISBN.class);
    }

    @Test
    @DisplayName("registerBooks: 여러 권의 도서를 한꺼번에 등록")
    void registerBooks_success() {
        // Given
        Book book = new Book();
        Map<String, Book> bookMap = Map.of("key1", book);
        Map<String, Long> categoryMap = Map.of("key1", 1L);
        Map<String, List<String>> tagMap = Map.of("key1", List.of("tag1"));
        Map<String, List<AuthorReqDTO>> authorMap = Map.of("key1", List.of());
        Map<String, String> publisherMap = Map.of("key1", "pub1");

        // When
        sut.registerBooks(bookMap, categoryMap, tagMap, authorMap, publisherMap);

        // Then
        verify(categoryService).assignCategoriesToBooks(eq(bookMap), anyMap());
        verify(tagService).assignTagsToBooks(eq(bookMap), anyMap());
        verify(authorService).assignAuthorsToBooks(eq(bookMap), anyMap());
        verify(publisherService).assignPublisherToBooks(eq(bookMap), anyMap());
        verify(bookRepository).saveAll(any());
    }

    @Test
    @DisplayName("updateBookByData: 모든 필드가 null일 때 변경이 일어나지 않는지 확인")
    void updateBookByData_noChanges_whenDtoIsNull() {
        Book book = new Book();
        book.setTitle("Original");
        book.setPrice(0L); // NPE 방지를 위해 기본값 세팅
        book.setStock(0);
        // ... 필요한 다른 필드들도 기본값 세팅 ...

        BookUpdateData emptyDto = mock(BookUpdateData.class);
        // mock은 기본적으로 null을 반환하므로 updateCheckDTO.price() != null 조건을 타지 않음

        // When
        sut.updateBookByData(book, emptyDto);

        // Then
        assertThat(book.getTitle()).isEqualTo("Original");
    }

    @Test
    @DisplayName("getBookDetail_Id: 결과가 null이면 NotFoundBookId 발생")
    void getBookDetail_Id_notFound_throws() {
        when(bookRepository.getBookDetailById(1L, false)).thenReturn(null);

        assertThatThrownBy(() -> sut.getBookDetail_Id(1L))
                .isInstanceOf(NotFoundBookId.class);
    }

    @Test
    @DisplayName("getBookUpdateView_Id: 결과가 null이면 NotFoundBookId 발생")
    void getBookUpdateView_Id_notFound_throws() {
        when(bookRepository.getBookUpdateView(1L)).thenReturn(null);

        assertThatThrownBy(() -> sut.getBookUpdateView_Id(1L))
                .isInstanceOf(NotFoundBookId.class);
    }

    @Test
    @DisplayName("getBookIdsFromBookOfTheWeek: limit과 pageable이 null일 때 기본값 처리 확인")
    void getBookIdsFromBookOfTheWeek_defaults() {
        // When
        sut.getBookIdsFromBookOfTheWeek(null, null);

        // Then: PageRequest.of(0, 10)으로 호출되었는지 검증
        verify(bookOfTheWeekRepository).getBookId(argThat(p ->
                p.getPageNumber() == 0 && p.getPageSize() == 10
        ));
    }

    @Test
    @DisplayName("deleteBook: Repository의 delete 호출 확인")
    void deleteBook_callsRepository() {
        Book book = new Book();
        sut.deleteBook(book);
        verify(bookRepository).delete(book);
    }

    @Test
    @DisplayName("getBookByCategoryIdList: 카테고리 ID 리스트로 조회 호출 확인")
    void getBookByCategoryIdList_callsRepository() {
        List<Long> ids = List.of(1L, 2L);
        sut.getBookByCategoryIdList(null, ids);
        verify(bookRepository).getBookByCategoryIdIn(any(), eq(ids), eq(false));
    }
}