package com.daisobook.shop.booksearch.BooksSearch.service.review;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.ImageMetadataReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.order.BookOrderDetailRequest;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.review.ReviewMetadataReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.review.ReviewReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.review.ReviewGroupReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.ReviewRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.order.BookResponse;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.order.BookReviewResponse;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.entity.review.Review;
import com.daisobook.shop.booksearch.BooksSearch.entity.review.ReviewImage;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.book.NotFoundBook;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.review.CannotChangedReview;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.review.DuplicatedReview;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.review.NotFoundReview;
import com.daisobook.shop.booksearch.BooksSearch.mapper.review.ReviewMapper;
import com.daisobook.shop.booksearch.BooksSearch.repository.review.ReviewRepository;
import com.daisobook.shop.booksearch.BooksSearch.service.image.impl.ReviewImageServiceImpl;
import com.daisobook.shop.booksearch.BooksSearch.service.review.impl.ReviewServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;

import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReviewServiceTest {

    @MockitoBean
    @Mock
    private ReviewRepository reviewRepository;

    @MockitoBean @Mock
    private ReviewImageServiceImpl imageService;

    @MockitoBean @Mock
    private ObjectMapper objectMapper;

    @MockitoBean @Mock
    private ReviewMapper reviewMapper;

    @Spy
    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Test
    @DisplayName("parsing: JSON 문자열을 ReviewReqDTO로 파싱함")
    void parsing2_parsesMetadataAndCollectsFiles() throws Exception {
        String json = "{\"some\":\"data\"}";
        ReviewReqDTO metadata = mock(ReviewReqDTO.class);
        when(objectMapper.readValue(eq(json), eq(ReviewReqDTO.class))).thenReturn(metadata);

        MultipartFile f0 = mock(MultipartFile.class);
        when(f0.getName()).thenReturn("image0");
        MultipartFile f1 = mock(MultipartFile.class);
        when(f1.getName()).thenReturn("image1");

        ReviewGroupReqDTO res = reviewService.parsing2(json, f0, f1, null);

        assertNotNull(res);
        assertEquals(metadata, res.reviewReqDTO());
        assertTrue(res.fileMap().containsKey("image0"));
        assertTrue(res.fileMap().containsKey("image1"));
        verify(objectMapper, times(1)).readValue(eq(json), eq(ReviewReqDTO.class));
    }

    @Test
    @DisplayName("parsing: null 입력 시 RuntimeException 발생")
    void parsing_nullDto_throwsRuntime() {
        assertThrows(RuntimeException.class, () -> reviewService.parsing(null));
    }

    @Test
    @DisplayName("registerReview: 중복 리뷰가 없으면 저장하고 이미지도 추가함")
    void registerReview_success_persistsAndAddsImages() throws Exception {
        ReviewReqDTO req = mock(ReviewReqDTO.class);
        when(req.bookId()).thenReturn(1L);
        when(req.userId()).thenReturn(2L);
        when(req.orderDetailId()).thenReturn(3L);
        when(req.imageMetadataReqDTOList()).thenReturn(Collections.emptyList());

        when(reviewRepository.existsReviewByBook_IdAndUserIdAndOderDetailId(anyLong(), anyLong(), anyLong())).thenReturn(false);

        Book book = mock(Book.class);
        List<Review> bookReviews = new ArrayList<>();
        when(book.getReviews()).thenReturn(bookReviews);
        when(book.getId()).thenReturn(1L);

        // 이미지 서비스가 반환하는 리스트
        ReviewImage ri = mock(ReviewImage.class);
        when(ri.getPath()).thenReturn("p");
        List<ReviewImage> imgList = List.of(ri);
        when(imageService.addReviewImage(any(), anyMap())).thenReturn(imgList);

        // save: capture argument and return it
        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        doAnswer(invocation -> invocation.getArgument(0)).when(reviewRepository).save(captor.capture());

        Review newReview = reviewService.registerReview(req, Collections.emptyMap(), book);

        assertNotNull(newReview);
        verify(reviewRepository, times(1)).save(any());
        verify(imageService, times(1)).addReviewImage(any(), anyMap());
        assertEquals(imgList, newReview.getReviewImages());
        assertTrue(bookReviews.contains(newReview));
    }

    @Test
    @DisplayName("registerReview: 중복 리뷰가 있으면 DuplicatedReview 예외 발생")
    void registerReview_duplicate_throws() {
        ReviewReqDTO req = mock(ReviewReqDTO.class);
        when(req.bookId()).thenReturn(1L);
        when(req.userId()).thenReturn(2L);
        when(req.orderDetailId()).thenReturn(3L);

        when(reviewRepository.existsReviewByBook_IdAndUserIdAndOderDetailId(anyLong(), anyLong(), anyLong())).thenReturn(true);

        assertThrows(DuplicatedReview.class, () -> reviewService.registerReview(req, Collections.emptyMap(), mock(Book.class)));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerReview: 도서가 없으면 NotFoundBook 예외 발생")
    void registerReview_missingBook_throwsNotFoundBook() {
        ReviewReqDTO req = mock(ReviewReqDTO.class);
        when(req.bookId()).thenReturn(1L);
        when(req.userId()).thenReturn(2L);
        when(req.orderDetailId()).thenReturn(3L);

        when(reviewRepository.existsReviewByBook_IdAndUserIdAndOderDetailId(anyLong(), anyLong(), anyLong())).thenReturn(false);

        assertThrows(NotFoundBook.class, () -> reviewService.registerReview(req, Collections.emptyMap(), null));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("getReviewById: 존재하는 리뷰는 매핑하여 반환하고, 없으면 NotFoundReview 예외 발생")
    void getReviewById_success_and_notFound() {
        Review r = mock(Review.class);
        Book b = mock(Book.class);
        when(b.getId()).thenReturn(10L);
        when(b.getTitle()).thenReturn("t");
        when(r.getId()).thenReturn(5L);
        when(r.getBook()).thenReturn(b);
        when(r.getUserId()).thenReturn(100L);
        when(r.getContent()).thenReturn("c");
        when(r.getRating()).thenReturn(4);
        when(r.getCreatedAt()).thenReturn(ZonedDateTime.now());
        when(r.getModifiedAt()).thenReturn(ZonedDateTime.now());
        ReviewImage ri = mock(ReviewImage.class);
        when(ri.getNo()).thenReturn(0);
        when(ri.getPath()).thenReturn("p");
        when(ri.getImageType()).thenReturn(null);
        when(r.getReviewImages()).thenReturn(List.of(ri));

        when(reviewRepository.findReviewById(5L)).thenReturn(r);

        ReviewRespDTO dto = reviewService.getReviewById(5L);
        assertNotNull(dto);
        assertEquals(5L, dto.id());

        when(reviewRepository.findReviewById(999L)).thenReturn(null);
        assertThrows(NotFoundReview.class, () -> reviewService.getReviewById(999L));
    }

    @Test
    @DisplayName("getReviewsByUserId / getReviewsByBookId: 각각 위임하고 매핑하여 반환함")
    void getReviewsByUserId_andByBookId_delegateAndMap() {
        Review r1 = mock(Review.class);
        Book b1 = mock(Book.class);
        when(b1.getId()).thenReturn(11L);
        when(b1.getTitle()).thenReturn("Book Eleven");
        when(r1.getId()).thenReturn(101L);
        when(r1.getBook()).thenReturn(b1);
        when(r1.getUserId()).thenReturn(11L);
        when(r1.getContent()).thenReturn("content1");
        when(r1.getRating()).thenReturn(5);
        when(r1.getCreatedAt()).thenReturn(ZonedDateTime.now());
        when(r1.getModifiedAt()).thenReturn(ZonedDateTime.now());
        ReviewImage ri1 = mock(ReviewImage.class);
        when(ri1.getNo()).thenReturn(0);
        when(ri1.getPath()).thenReturn("p1");
        when(ri1.getImageType()).thenReturn(null);
        when(r1.getReviewImages()).thenReturn(List.of(ri1));

        Review r2 = mock(Review.class);
        Book b2 = mock(Book.class);
        when(b2.getId()).thenReturn(22L);
        when(b2.getTitle()).thenReturn("Book TwentyTwo");
        when(r2.getId()).thenReturn(202L);
        when(r2.getBook()).thenReturn(b2);
        when(r2.getUserId()).thenReturn(33L);
        when(r2.getContent()).thenReturn("content2");
        when(r2.getRating()).thenReturn(3);
        when(r2.getCreatedAt()).thenReturn(ZonedDateTime.now());
        when(r2.getModifiedAt()).thenReturn(ZonedDateTime.now());
        ReviewImage ri2 = mock(ReviewImage.class);
        when(ri2.getNo()).thenReturn(0);
        when(ri2.getPath()).thenReturn("p2");
        when(ri2.getImageType()).thenReturn(null);
        when(r2.getReviewImages()).thenReturn(List.of(ri2));

        when(reviewRepository.findAllByUserId(11L)).thenReturn(List.of(r1));
        when(reviewRepository.findAllByBook_Id(22L)).thenReturn(List.of(r2));

        List<ReviewRespDTO> byUser = reviewService.getReviewsByUserId(11L);
        List<ReviewRespDTO> byBook = reviewService.getReviewsByBookId(22L);

        assertNotNull(byUser);
        assertEquals(1, byUser.size());
        ReviewRespDTO dtoUser = byUser.getFirst();
        assertEquals(101L, dtoUser.id());
        assertEquals(11L, dtoUser.bookId());

        assertNotNull(byBook);
        assertEquals(1, byBook.size());
        ReviewRespDTO dtoBook = byBook.getFirst();
        assertEquals(202L, dtoBook.id());
        assertEquals(22L, dtoBook.bookId());
    }

    @Test
    @DisplayName("updateReview: 존재하지 않는 리뷰는 NotFoundReview 예외 발생")
    void updateReview_notFound_throws() {
        when(reviewRepository.findReviewById(1L)).thenReturn(null);
        ReviewReqDTO req = mock(ReviewReqDTO.class);
        assertThrows(NotFoundReview.class, () -> reviewService.updateReview(1L, req, Collections.emptyMap()));
    }

    @Test
    @DisplayName("updateReview: 변경사항이 없으면 CannotChangedReview 예외 발생")
    void updateReview_invalidRating_throwsCannotChangedReview() {
        Review r = mock(Review.class);
        Book b = mock(Book.class);
        when(b.getId()).thenReturn(10L);
        when(r.getBook()).thenReturn(b);
        when(r.getUserId()).thenReturn(20L);
        when(r.getOderDetailId()).thenReturn(30L);
        when(r.getContent()).thenReturn("same");
        when(r.getRating()).thenReturn(2);

        when(reviewRepository.findReviewById(100L)).thenReturn(r);

        ReviewReqDTO req = mock(ReviewReqDTO.class);
        when(req.bookId()).thenReturn(10L);
        when(req.userId()).thenReturn(20L);
        when(req.orderDetailId()).thenReturn(30L);
        when(req.content()).thenReturn("same");
        when(req.rating()).thenReturn(9); // invalid rating

        assertThrows(CannotChangedReview.class, () -> reviewService.updateReview(100L, req, Collections.emptyMap()));
    }

    @Test
    @DisplayName("updateReview: 이미지 변경사항이 감지되면 이미지 서비스 호출함")
    void updateReview_detectsImageChange_and_callsImageService() {
        Review r = mock(Review.class);
        Book b = mock(Book.class);
        when(b.getId()).thenReturn(1L);
        when(r.getBook()).thenReturn(b);
        when(r.getUserId()).thenReturn(2L);
        when(r.getOderDetailId()).thenReturn(3L);
        when(r.getContent()).thenReturn("c");
        when(r.getRating()).thenReturn(3);

        ReviewImage existing = mock(ReviewImage.class);
        when(existing.getNo()).thenReturn(0);
        when(existing.getPath()).thenReturn("old");
        when(r.getReviewImages()).thenReturn(new ArrayList<>(List.of(existing)));
        when(reviewRepository.findReviewById(50L)).thenReturn(r);

        ImageMetadataReqDTO meta0 = mock(ImageMetadataReqDTO.class);
        when(meta0.sequence()).thenReturn(0);
        when(meta0.dataUrl()).thenReturn("new-url");
        when(meta0.fileKey()).thenReturn(null);

        ReviewReqDTO req = mock(ReviewReqDTO.class);
        when(req.bookId()).thenReturn(1L);
        when(req.userId()).thenReturn(2L);
        when(req.orderDetailId()).thenReturn(3L);
        when(req.content()).thenReturn("c");
        when(req.rating()).thenReturn(3);
        when(req.imageMetadataReqDTOList()).thenReturn(List.of(meta0));

        // imageService.updateReviewImage returns a list containing updated existing and a newly created one
        ReviewImage updatedExisting = mock(ReviewImage.class);
        when(updatedExisting.getNo()).thenReturn(0);
        when(updatedExisting.getPath()).thenReturn("new-url");
        when(imageService.updateReviewImage(any(), anyMap())).thenReturn(List.of(updatedExisting));

        // call
        reviewService.updateReview(50L, req, Collections.emptyMap());

        verify(imageService, times(1)).updateReviewImage(any(), anyMap());
        verify(r, times(1)).setReviewImages(anyList());
    }

    @Test
    @DisplayName("updateReview: 이미지 변경사항이 없으면 이미지 서비스 호출하지 않음")
    void findReviewByUserIdAndBookIdAndOrderDetailId_delegates() {
        Review r = mock(Review.class);
        when(reviewRepository.findReviewByUserIdAndBook_IdAndOderDetailId(1L, 2L, 3L)).thenReturn(r);
        Review res = reviewService.findReviewByUserIdAndBookIdAndOrderDetailId(1L,2L,3L);
        assertEquals(r, res);
    }

    @Test
    @DisplayName("findBookReviewList: 매핑 및 JSON 매핑 오류 처리")
    void findBookReviewList_mapsAndHandlesJsonMappingError() throws Exception {
        BookOrderDetailRequest req = mock(BookOrderDetailRequest.class);
        when(req.bookId()).thenReturn(11L);
        when(req.orderDetailId()).thenReturn(22L);

        // repository projection return
        when(reviewRepository.getBookReviewProjectionList(anyLong(), anyList(), anyList())).thenReturn(Collections.emptyList());

        // happy case: mapper returns list
        when(reviewMapper.toBookReviewResponseList(anyList())).thenReturn(List.of(new BookReviewResponse(new BookResponse(11L, "test", List.of()),22L, 1L)));
        List<BookReviewResponse> out = reviewService.findBookReviewList(1L, List.of(req));
        assertNotNull(out);

        // error case: mapper throws JsonProcessingException
        when(reviewRepository.getBookReviewProjectionList(anyLong(), anyList(), anyList())).thenReturn(Collections.emptyList());
        when(reviewMapper.toBookReviewResponseList(anyList())).thenThrow(new JsonProcessingException("fail") {});
        assertThrows(RuntimeException.class, () -> reviewService.findBookReviewList(1L, List.of(req)));
    }

    @Test
    @DisplayName("getCountByRelease: 리포지토리에 위임하여 개수 조회함")
    void getCountByRelease_delegatesToRepository() {
        ZonedDateTime dt = ZonedDateTime.now();
        when(reviewRepository.countAllByCreatedAtAfterOrModifiedAtAfter(any(), any())).thenReturn(123L);
        Long c = reviewService.getCountByRelease(7);
        assertEquals(123L, c);
        verify(reviewRepository, times(1)).countAllByCreatedAtAfterOrModifiedAtAfter(any(), any());
    }
}
