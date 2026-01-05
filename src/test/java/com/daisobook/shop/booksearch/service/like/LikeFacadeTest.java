package com.daisobook.shop.booksearch.service.like;


import com.daisobook.shop.booksearch.dto.DiscountDTO;
import com.daisobook.shop.booksearch.dto.response.like.LikeListRespDTO;
import com.daisobook.shop.booksearch.dto.response.like.MyLikeList;
import com.daisobook.shop.booksearch.dto.projection.LikeBookListProjection;
import com.daisobook.shop.booksearch.entity.book.Book;
import com.daisobook.shop.booksearch.exception.custom.book.NotFoundBook;
import com.daisobook.shop.booksearch.exception.custom.mapper.FailObjectMapper;
import com.daisobook.shop.booksearch.mapper.like.LikeMapper;
import com.daisobook.shop.booksearch.service.book.impl.BookCoreService;
import com.daisobook.shop.booksearch.service.like.impl.LikeFacade;
import com.daisobook.shop.booksearch.service.policy.DiscountPolicyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LikeFacadeTest {

    @MockitoBean
    @Mock
    private LikeService likeService;

    @MockitoBean @Mock
    private BookCoreService bookService;

    @MockitoBean @Mock
    private DiscountPolicyService discountPolicyService;

    @MockitoBean @Mock
    private LikeMapper likeMapper;

    @InjectMocks
    private LikeFacade likeFacade; // 테스트 대상

    private final long SAMPLE_BOOK_ID = 123L;
    private final long SAMPLE_USER_ID = 77L;

    @BeforeEach
    void setup() {
        // 기본 동작은 필요 시 각 테스트에서 오버라이드
    }

    @Test
    @DisplayName("addLike: 정상 동작 시 saveLike 호출 확인")
    void addLike_success_callsSaveLike() {
        Book mockBook = mock(Book.class);
        when(mockBook.getId()).thenReturn(SAMPLE_BOOK_ID);
        when(bookService.getBook_Id(SAMPLE_BOOK_ID)).thenReturn(mockBook);
        // existLike 는 기본적으로 아무일도 하지 않음 (정상 시)
        doNothing().when(likeService).existLike(SAMPLE_BOOK_ID, SAMPLE_USER_ID);

        likeFacade.addLike(SAMPLE_BOOK_ID, SAMPLE_USER_ID);

        // saveLike 가 호출되어 새 Like가 저장되었는지 확인
        verify(likeService, times(1)).saveLike(argThat(l -> l != null && l.getBook() != null && l.getUserId() == SAMPLE_USER_ID));
    }

    @Test
    @DisplayName("addLike: 도서가 존재하지 않을 때 NotFoundBook 예외 발생")
    void addLike_whenBookNotFound_throwsNotFoundBook() {
        when(bookService.getBook_Id(SAMPLE_BOOK_ID)).thenReturn(null);
        doNothing().when(likeService).existLike(SAMPLE_BOOK_ID, SAMPLE_USER_ID);

        NotFoundBook ex = assertThrows(NotFoundBook.class, () -> likeFacade.addLike(SAMPLE_BOOK_ID, SAMPLE_USER_ID));
        assertTrue(ex.getMessage().contains("존재하지 않는"));
        verify(likeService, never()).saveLike(any());
    }

    @Test
    @DisplayName("addLike: 이미 존재하는 좋아요일 때 RuntimeException 전파")
    void addLike_whenExistLikeThrows_propagatesException() {
        doThrow(new RuntimeException("이미 존재하는 좋아요 입니다.")).when(likeService).existLike(SAMPLE_BOOK_ID, SAMPLE_USER_ID);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> likeFacade.addLike(SAMPLE_BOOK_ID, SAMPLE_USER_ID));
        assertEquals("이미 존재하는 좋아요 입니다.", ex.getMessage());
        verify(bookService, never()).getBook_Id(anyLong());
        verify(likeService, never()).saveLike(any());
    }

    @Test
    @DisplayName("getMyLikeList: 정상 동작 시 매핑된 MyLikeList 반환")
    void getMyLikeList_success_returnsMappedMyLikeList() throws Exception {
        Pageable anyPage = PageRequest.of(0, 10);
        List<LikeBookListProjection> projections = List.of(mock(LikeBookListProjection.class));
        when(likeService.getMyLikeList(eq(SAMPLE_USER_ID), any(Pageable.class))).thenReturn(projections);

        Map<Long, DiscountDTO.Request> discountReqMap = Map.of(1L, new DiscountDTO.Request(1L, 0L));
        when(likeMapper.toDiscountDTOMap(projections)).thenReturn(discountReqMap);

        Map<Long, DiscountDTO.Response> discountRespMap = Map.of(1L, new DiscountDTO.Response(1L, 0L, BigDecimal.ZERO, 0L));
        when(discountPolicyService.getDiscountPriceMap(discountReqMap)).thenReturn(discountRespMap);

        LikeListRespDTO mockedRespDto = mock(LikeListRespDTO.class);
        List<LikeListRespDTO> respList = List.of(mockedRespDto);
        when(likeMapper.toLikeListRespDTOList(projections, discountRespMap)).thenReturn(respList);

        MyLikeList result = likeFacade.getMyLikeList(SAMPLE_USER_ID);
        assertNotNull(result);
        assertEquals(1, result.likeList().size());
    }

    @Test
    @DisplayName("getMyLikeList: 할인 정책 매핑 중 예외 발생 시 FailObjectMapper 예외로 변환")
    void getMyLikeList_whenDiscountPolicyThrows_translatesToFailObjectMapper() throws Exception {
        List<LikeBookListProjection> projections = List.of(mock(LikeBookListProjection.class));
        when(likeService.getMyLikeList(eq(SAMPLE_USER_ID), any(Pageable.class))).thenReturn(projections);
        when(likeMapper.toDiscountDTOMap(projections)).thenReturn(Collections.emptyMap());

        when(discountPolicyService.getDiscountPriceMap(anyMap())).thenThrow(new JsonProcessingException("err"){});

        FailObjectMapper ex = assertThrows(FailObjectMapper.class, () -> likeFacade.getMyLikeList(SAMPLE_USER_ID));
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("getMyLikeList: 할인 가격 맵이 null일 때 null 반환")
    void getMyLikeList_whenDiscountPriceMapIsNull_returnsNull() throws Exception {
        List<LikeBookListProjection> projections = List.of(mock(LikeBookListProjection.class));
        when(likeService.getMyLikeList(eq(SAMPLE_USER_ID), any(Pageable.class))).thenReturn(projections);
        when(likeMapper.toDiscountDTOMap(projections)).thenReturn(Collections.emptyMap());
        when(discountPolicyService.getDiscountPriceMap(anyMap())).thenReturn(null);

        MyLikeList res = likeFacade.getMyLikeList(SAMPLE_USER_ID);
        assertNull(res);
    }

    @Test
    @DisplayName("getMyLikeList: 매핑 중 예외 발생 시 FailObjectMapper 예외로 변환")
    void getMyLikeList_whenMapperThrows_translatesToFailObjectMapper() throws Exception {
        List<LikeBookListProjection> projections = List.of(mock(LikeBookListProjection.class));
        when(likeService.getMyLikeList(eq(SAMPLE_USER_ID), any(Pageable.class))).thenReturn(projections);
        when(likeMapper.toDiscountDTOMap(projections)).thenReturn(Collections.emptyMap());

        Map<Long, DiscountDTO.Response> discountRespMap = Map.of();
        when(discountPolicyService.getDiscountPriceMap(anyMap())).thenReturn(discountRespMap);

        when(likeMapper.toLikeListRespDTOList(projections, discountRespMap)).thenThrow(new JsonProcessingException("mapErr"){});

        FailObjectMapper ex = assertThrows(FailObjectMapper.class, () -> likeFacade.getMyLikeList(SAMPLE_USER_ID));
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("deleteLike: 정상 위임 및 예외 전파 확인")
    void deleteLike_delegatesToService_andPropagatesExceptions() {
        // 정상 위임 확인
        doNothing().when(likeService).deleteLike(SAMPLE_BOOK_ID, SAMPLE_USER_ID);
        likeFacade.deleteLike(SAMPLE_BOOK_ID, SAMPLE_USER_ID);
        verify(likeService, times(1)).deleteLike(SAMPLE_BOOK_ID, SAMPLE_USER_ID);

        // 예외 전파 확인
        doThrow(new RuntimeException("delete-failed")).when(likeService).deleteLike(SAMPLE_BOOK_ID, SAMPLE_USER_ID);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> likeFacade.deleteLike(SAMPLE_BOOK_ID, SAMPLE_USER_ID));
        assertEquals("delete-failed", ex.getMessage());
    }
}
