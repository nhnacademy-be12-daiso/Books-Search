package com.daisobook.shop.booksearch.books_search.service.like;

import com.daisobook.shop.booksearch.books_search.dto.projection.LikeBookListProjection;
import com.daisobook.shop.booksearch.books_search.entity.book.Book;
import com.daisobook.shop.booksearch.books_search.entity.like.Like;
import com.daisobook.shop.booksearch.books_search.exception.custom.book.NotFoundBook;
import com.daisobook.shop.booksearch.books_search.exception.custom.like.ExistedLike;
import com.daisobook.shop.booksearch.books_search.repository.like.LikeRepository;
import com.daisobook.shop.booksearch.books_search.service.like.impl.LikeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LikeServiceTest {

    @MockitoBean
    @Mock
    private LikeRepository likeRepository;

    @InjectMocks
    private LikeServiceImpl likeService;

    private final long SAMPLE_BOOK_ID = 11L;
    private final long SAMPLE_USER_ID = 22L;

    @BeforeEach
    void setUp() {
        // 테스트별로 스텁을 명확히 설정하도록 비워둠
    }

    @Test
    @DisplayName("existLike: 좋아요가 이미 존재할 때 RuntimeException 발생")
    void existLike_whenExists_throwsRuntimeException() {
        when(likeRepository.existsLikeByBookIdAndUserId(SAMPLE_BOOK_ID, SAMPLE_USER_ID)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> likeService.existLike(SAMPLE_BOOK_ID, SAMPLE_USER_ID));
        assertEquals("이미 존재하는 좋아요 입니다.", ex.getMessage());
        verify(likeRepository, times(1)).existsLikeByBookIdAndUserId(SAMPLE_BOOK_ID, SAMPLE_USER_ID);
    }

    @Test
    @DisplayName("existLike: 좋아요가 존재하지 않을 때 예외 발생하지 않음")
    void existLike_whenNotExists_noException() {
        when(likeRepository.existsLikeByBookIdAndUserId(SAMPLE_BOOK_ID, SAMPLE_USER_ID)).thenReturn(false);

        assertDoesNotThrow(() -> likeService.existLike(SAMPLE_BOOK_ID, SAMPLE_USER_ID));
    }

    @Test
    @DisplayName("createLike: 도서가 null일 때 NotFoundBook 예외 발생")
    void createLike_whenBookNull_throwsNotFoundBook() {
        NotFoundBook ex = assertThrows(NotFoundBook.class, () -> likeService.createLike(SAMPLE_USER_ID, null));
        assertTrue(ex.getMessage().contains("존재하지 않는 도서의 좋아요 시도입니다."));
    }

    @Test
    @DisplayName("createLike: 성공 시, LikeRepository의 save 메서드 호출")
    void createLike_success_callsRepositorySave() {
        Book book = mock(Book.class);
        when(book.getId()).thenReturn(SAMPLE_BOOK_ID);
        when(likeRepository.existsLikeByBookIdAndUserId(SAMPLE_BOOK_ID, SAMPLE_USER_ID)).thenReturn(false);

        likeService.createLike(SAMPLE_USER_ID, book);

        ArgumentCaptor<Like> captor = ArgumentCaptor.forClass(Like.class);
        verify(likeRepository, times(1)).save(captor.capture());
        Like saved = captor.getValue();
        assertNotNull(saved);
        assertEquals(SAMPLE_USER_ID, saved.getUserId());
        assertEquals(book, saved.getBook());
    }

    @Test
    @DisplayName("getLikeList: 좋아요 목록이 null일 때 빈 리스트 반환")
    void getLikeList_whenRepositoryReturnsNull_returnsEmptyList() {
        when(likeRepository.findAllByUserId(SAMPLE_USER_ID)).thenReturn(null);

        List<?> res = likeService.getLikeList(SAMPLE_USER_ID);
        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    @Test
    @DisplayName("getLikeList: 좋아요 목록이 존재할 때 DTO 리스트 반환")
    void getLikeList_whenRepositoryReturnsList_returnsDtoListSize() {
        Like like = mock(Like.class);
        Book book = mock(Book.class, RETURNS_DEEP_STUBS);

        when(like.getId()).thenReturn(100L);
        when(like.getBook()).thenReturn(book);
        when(book.getId()).thenReturn(200L);
        when(like.getUserId()).thenReturn(SAMPLE_USER_ID);
        when(book.getTitle()).thenReturn("T");
        when(book.getBookImages().getFirst().getPath()).thenReturn("image-path");

        when(likeRepository.findAllByUserId(SAMPLE_USER_ID)).thenReturn(List.of(like));

        List<?> res = likeService.getLikeList(SAMPLE_USER_ID);
        assertNotNull(res);
        assertEquals(1, res.size());
    }


    @Test
    @DisplayName("likeCount: 좋아요 개수 조회가 Repository에 위임됨")
    void likeCount_delegatesToRepository() {
        when(likeRepository.countAllByBook_Id(SAMPLE_BOOK_ID)).thenReturn(5);
        int cnt = likeService.likeCount(SAMPLE_BOOK_ID);
        assertEquals(5, cnt);
    }

    @Test
    @DisplayName("likeCheck: userId가 null일 때 false 반환")
    void likeCheck_whenUserNull_returnsFalse() {
        boolean res = likeService.likeCheck(SAMPLE_BOOK_ID, null);
        assertFalse(res);
    }

    @Test
    @DisplayName("likeCheck: 좋아요가 존재할 때 true 반환")
    void likeCheck_whenExists_returnsTrue() {
        when(likeRepository.existsLikeByBook_IdAndUserId(SAMPLE_BOOK_ID, SAMPLE_USER_ID)).thenReturn(true);
        boolean res = likeService.likeCheck(SAMPLE_BOOK_ID, SAMPLE_USER_ID);
        assertTrue(res);
    }

    @Test
    @DisplayName("likeCheck: 좋아요가 존재하지 않을 때 false 반환")
    void getLikeByUserIdAndBookIds_whenUserNull_returnsNull() {
        Set<Long> res = likeService.getLikeByUserIdAndBookIds(null, List.of(1L,2L));
        assertNull(res);
    }

    @Test
    @DisplayName("getLikeByUserIdAndBookIds: Repository가 빈 리스트 반환할 때 null 반환")
    void getLikeByUserIdAndBookIds_whenRepoReturnsEmpty_returnsNull() {
        when(likeRepository.getLikeByUserIdAndBookIdIn(SAMPLE_USER_ID, List.of(1L,2L))).thenReturn(null);
        Set<Long> res = likeService.getLikeByUserIdAndBookIds(SAMPLE_USER_ID, List.of(1L,2L));
        assertNull(res);
    }

    @Test
    @DisplayName("getBookIsLike: userId가 null일 때 빈 리스트 반환")
    void getBookIsLike_whenUserNull_returnsEmptyList() {
        List<Like> res = likeService.getBookIsLike(null, List.of(mock(Book.class)));
        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    @Test
    @DisplayName("getBookIsLike: Repository에 위임됨")
    void getBookIsLike_delegatesToRepository() {
        Book b = mock(Book.class);
        List<Like> expected = List.of(mock(Like.class));
        when(likeRepository.findAllByUserIdAndBookIn(SAMPLE_USER_ID, List.of(b))).thenReturn(expected);

        List<Like> res = likeService.getBookIsLike(SAMPLE_USER_ID, List.of(b));
        assertEquals(expected, res);
    }

    @Test
    @DisplayName("deleteLike: 도서가 null일 때 NotFoundBook 예외 발생")
    void deleteLike_withNullBook_throwsNotFoundBook() {
        NotFoundBook ex = assertThrows(NotFoundBook.class, () -> likeService.deleteLike(SAMPLE_USER_ID, null));
        assertTrue(ex.getMessage().contains("존재하지 않는 도서의 좋아요 취소입니다."));
    }

    @Test
    @DisplayName("saveLike: Repository의 save 메서드 호출")
    void saveLike_callsRepositorySave() {
        Like like = mock(Like.class);
        likeService.saveLike(like);
        verify(likeRepository, times(1)).save(like);
    }

    @Test
    @DisplayName("deleteLike: 좋아요가 존재하지 않을 때 ExistedLike 예외 발생")
    void deleteLike_byIds_whenNotFound_throwsExistedLike() {
        when(likeRepository.findLikeByBook_IdAndUserId(SAMPLE_BOOK_ID, SAMPLE_USER_ID)).thenReturn(null);
        ExistedLike ex = assertThrows(ExistedLike.class, () -> likeService.deleteLike(SAMPLE_BOOK_ID, SAMPLE_USER_ID));
        assertTrue(ex.getMessage().contains("존재하지 않는 좋아요"));
    }

    @Test
    @DisplayName("deleteLike: 좋아요가 존재할 때 삭제 수행")
    void deleteLike_byIds_success_deletesFoundLike() {
        Like found = mock(Like.class);
        when(likeRepository.findLikeByBook_IdAndUserId(SAMPLE_BOOK_ID, SAMPLE_USER_ID)).thenReturn(found);
        likeService.deleteLike(SAMPLE_BOOK_ID, SAMPLE_USER_ID);
        verify(likeRepository, times(1)).delete(found);
    }

    @Test
    @DisplayName("getMyLikeList: Repository에 위임됨")
    void getMyLikeList_delegatesToRepository() {
        List<?> mockRes = List.of();
        when(likeRepository.getAllByUserId(SAMPLE_USER_ID)).thenReturn((List<LikeBookListProjection>) mockRes);
        List<?> res = likeService.getMyLikeList(SAMPLE_USER_ID, null);
        assertEquals(mockRes, res);
    }
}
