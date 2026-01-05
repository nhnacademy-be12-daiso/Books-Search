package com.daisobook.shop.booksearch.service.book.impl;

import com.daisobook.shop.booksearch.dto.BookUpdateData;
import com.daisobook.shop.booksearch.dto.BookListData;
import com.daisobook.shop.booksearch.dto.DiscountDTO;
import com.daisobook.shop.booksearch.dto.point.PointPolicyType;
import com.daisobook.shop.booksearch.dto.projection.*;
import com.daisobook.shop.booksearch.dto.request.AuthorReqDTO;
import com.daisobook.shop.booksearch.dto.request.ImageMetadataReqDTO;
import com.daisobook.shop.booksearch.dto.request.TagReqDTO;
import com.daisobook.shop.booksearch.dto.request.book.BookGroupReqV2DTO;
import com.daisobook.shop.booksearch.dto.request.book.BookReqV2DTO;
import com.daisobook.shop.booksearch.dto.request.order.OrderCancelRequest;
import com.daisobook.shop.booksearch.dto.request.review.ReviewReqDTO;
import com.daisobook.shop.booksearch.dto.service.ImagesReqDTO;
import com.daisobook.shop.booksearch.entity.BookListType;
import com.daisobook.shop.booksearch.entity.ImageType;
import com.daisobook.shop.booksearch.entity.book.Book;
import com.daisobook.shop.booksearch.entity.book.BookImage;
import com.daisobook.shop.booksearch.entity.book.Status;
import com.daisobook.shop.booksearch.entity.review.Review;
import com.daisobook.shop.booksearch.entity.review.ReviewImage;
import com.daisobook.shop.booksearch.dto.response.book.BookListRespDTO;
import com.daisobook.shop.booksearch.dto.response.book.BookUpdateView;
import com.daisobook.shop.booksearch.dto.response.order.OrderBookSummeryDTO;
import com.daisobook.shop.booksearch.dto.response.order.OrderBooksInfoRespDTO;
import com.daisobook.shop.booksearch.exception.custom.book.DuplicatedBook;
import com.daisobook.shop.booksearch.exception.custom.book.NotFoundBookISBN;
import com.daisobook.shop.booksearch.exception.custom.book.NotFoundBookId;
import com.daisobook.shop.booksearch.exception.custom.book.NotFoundBook;
import com.daisobook.shop.booksearch.exception.custom.mapper.FailObjectMapper;
import com.daisobook.shop.booksearch.mapper.book.BookMapper;
import com.daisobook.shop.booksearch.mapper.image.ImageMapper;
import com.daisobook.shop.booksearch.search.component.BookSearchSyncPublisher;
import com.daisobook.shop.booksearch.service.category.CategoryV2Service;
import com.daisobook.shop.booksearch.service.image.impl.BookImageServiceImpl;
import com.daisobook.shop.booksearch.service.like.LikeService;
import com.daisobook.shop.booksearch.service.policy.DiscountPolicyService;
import com.daisobook.shop.booksearch.service.review.ReviewService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class BookFacadeTest {

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("Unit tests with MockitoExtension + InjectMocks")
    class UnitTests {

        @Mock BookCoreService bookCoreService;
        @Mock BookImageServiceImpl imageService;
        @Mock LikeService likeService;
        @Mock DiscountPolicyService discountPolicyService;
        @Mock ReviewService reviewService;
        @Mock CategoryV2Service categoryService;
        @Mock BookMapper bookMapper;
        @Mock ImageMapper imageMapper;
        @Mock BookSearchSyncPublisher bookSearchSyncPublisher;

        @InjectMocks BookFacade bookFacade;

        @Test
        @DisplayName("parsing: metadata null -> RuntimeException")
        void parsing_nullMetadata_throws() throws JsonProcessingException {
            assertThatThrownBy(() -> bookFacade.parsing(null, null, null, null, null, null))
                    .isInstanceOf(RuntimeException.class);

            bookFacade.parsing("{}", null, null, null, null, null);
            verify(bookMapper).parsing(anyString(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("parsing: 정상 호출 -> mapper 호출")
        void parsing_success() throws Exception {
            BookGroupReqV2DTO dto = mock(BookGroupReqV2DTO.class);
            when(bookMapper.parsing(eq("json"), any(), any(), any(), any(), any())).thenReturn(dto);

            BookGroupReqV2DTO result = bookFacade.parsing("json", null, null, null, null, null);
            assertThat(result).isSameAs(dto);
            verify(bookMapper).parsing(eq("json"), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("registerBook: 정상 흐름")
        void registerBook_success() {
            BookReqV2DTO req = mock(BookReqV2DTO.class);
            when(req.isbn()).thenReturn("ISBN-1");

            Book book = new Book();
            book.setIsbn("ISBN-1");
            // 필요시 book.setId(1L); 를 추가해도 됨

            when(bookMapper.create(req)).thenReturn(book);
            // doReturn 으로 stubbing 하여 Strict stubbing 문제 회피, 마지막 인자는 nullable로 허용
            doReturn(book).when(bookCoreService)
                    .registerBook(any(), anyLong(), anyList(), anyList(), org.mockito.ArgumentMatchers.nullable(String.class));

            ImagesReqDTO imagesReq = mock(ImagesReqDTO.class);
            // id 매처를 anyLong()으로 변경하여 실제 호출값과 매칭되게 함
            when(imageMapper.createImagesReqDTO(anyLong(), anyList())).thenReturn(imagesReq);

            bookFacade.registerBook(req, Map.of());

            verify(bookCoreService).validateNotExistsByIsbn("ISBN-1");
            verify(imageService).addBookImage(eq(book), eq(imagesReq), anyMap());
            verify(bookSearchSyncPublisher).publishUpsertAfterCommit(eq(book), eq("BOOK_REGISTER"));
        }


        @Test
        @DisplayName("registerBooks: 중복 없을 때 등록 및 이미지 추가")
        void registerBooks_success() {
            BookReqV2DTO r1 = mock(BookReqV2DTO.class);
            when(r1.isbn()).thenReturn("A");
            when(r1.categoryId()).thenReturn(10L);
            when(r1.tags()).thenReturn(List.of(mock(TagReqDTO.class)));
            when(r1.authorReqDTOList()).thenReturn(List.of(mock(AuthorReqDTO.class)));
            when(r1.publisher()).thenReturn("P");
            when(r1.imageMetadataReqDTOList()).thenReturn(List.of(mock(ImageMetadataReqDTO.class)));

            List<BookReqV2DTO> list = List.of(r1);

            // 이미 존재하는 ISBN 없음
            when(bookCoreService.getExistsByIsbn(anyList())).thenReturn(Collections.emptySet());

            // bookMapper.create를 반드시 스텁하여 null 전달을 방지
            Book createdBook = new Book();
            createdBook.setIsbn("A");
            when(bookMapper.create(r1)).thenReturn(createdBook);

            Map<String, Book> created = Map.of("A", createdBook);
            when(bookCoreService.registerBooks(anyMap(), anyMap(), anyMap(), anyMap(), anyMap())).thenReturn(created);

            // imageMapper는 어떤 맵이든 빈 리스트 반환
            when(imageMapper.createImagesReqDTOList(anyMap(), anyMap())).thenReturn(List.of());

            bookFacade.registerBooks(list);

            verify(bookCoreService).registerBooks(anyMap(), anyMap(), anyMap(), anyMap(), anyMap());
            verify(imageService).addBookImages(eq(created), anyList());
        }

        @Test
        @DisplayName("registerBooks: 중복 ISBN이 섞여 있을 때 continue 분기 커버")
        void registerBooks_withDuplicates_test() {
            BookReqV2DTO dupReq = mock(BookReqV2DTO.class);
            when(dupReq.isbn()).thenReturn("DUP");
            BookReqV2DTO newReq = mock(BookReqV2DTO.class);
            when(newReq.isbn()).thenReturn("NEW");
            when(newReq.tags()).thenReturn(List.of());

            // DUP은 이미 존재한다고 설정
            given(bookCoreService.getExistsByIsbn(anyList())).willReturn(Set.of(() -> "DUP"));

            Book newBook = new Book();
            newBook.setIsbn("NEW");
            given(bookMapper.create(newReq)).willReturn(newBook);
            given(bookCoreService.registerBooks(anyMap(), anyMap(), anyMap(), anyMap(), anyMap())).willReturn(Map.of("NEW", newBook));

            bookFacade.registerBooks(List.of(dupReq, newReq));

            // 맵에 'NEW' 하나만 담겨서 서비스로 넘어갔는지 확인 (DUP은 continue 됨)
            verify(bookCoreService).registerBooks(argThat(m -> m.size() == 1), any(), any(), any(), any());
        }


        @Test
        @DisplayName("updateBook: 도서 없음 -> NotFoundBookId")
        void updateBook_notFound_throws() {
            when(bookCoreService.getBook_Id(123L)).thenReturn(null);
            assertThatThrownBy(() -> bookFacade.updateBook(123L, mock(BookReqV2DTO.class), Map.of()))
                    .isInstanceOf(NotFoundBookId.class);
        }

        @Test
        @DisplayName("updateBook: 이미지 변경 감지 시 updateBookImage 호출")
        void updateBook_imageChange_updates() {
            Book book = new Book();
            Long id=book.getId();
            BookImage bi = new BookImage();
            bi.setNo(0);
            bi.setPath("old");
            book.setBookImages(new ArrayList<>(List.of(bi)));
            when(bookCoreService.getBook_Id(id)).thenReturn(book);

            BookReqV2DTO req = mock(BookReqV2DTO.class);
            ImageMetadataReqDTO im = mock(ImageMetadataReqDTO.class);
            when(im.sequence()).thenReturn(0);
            when(im.dataUrl()).thenReturn("newUrl");
            when(im.fileKey()).thenReturn(null);
            when(req.imageMetadataReqDTOList()).thenReturn(List.of(im));

            BookUpdateData updateData = new BookUpdateData(
                    null, null, null, List.of(), null, null, null, null, null, null, null, 0L, List.of(), false
            );
            when(bookMapper.toBookUpdateData(req)).thenReturn(updateData);

            // imageMapper가 null을 반환하지 않도록 명시적으로 스텁
            ImagesReqDTO imagesReq = mock(ImagesReqDTO.class);
            when(imageMapper.createImagesReqDTO(eq(id), anyList())).thenReturn(imagesReq);

            when(bookCoreService.updateBookByData(any(), any())).thenReturn(book);

            Map<String, MultipartFile> fileMap = Map.of();
            bookFacade.updateBook(id, req, fileMap);

            verify(imageService).updateBookImage(eq(book), any(ImagesReqDTO.class), eq(fileMap));
        }

        @Test
        @DisplayName("updateBook: 이미지 변화가 전혀 없을 때 updateBookImage 미호출")
        void updateBook_noChange_skipsUpdate() {
            long bookId = 1L;
            Book book = new Book();
            BookImage bi = new BookImage(); bi.setNo(0); bi.setPath("same-url");
            book.setBookImages(new ArrayList<>(List.of(bi)));

            given(bookCoreService.getBook_Id(bookId)).willReturn(book);
            given(bookMapper.toBookUpdateData(any())).willReturn(mock(BookUpdateData.class));
            given(bookCoreService.updateBookByData(any(), any())).willReturn(book);

            BookReqV2DTO req = mock(BookReqV2DTO.class);
            // 동일한 URL 설정
            ImageMetadataReqDTO meta = new ImageMetadataReqDTO(0, ImageType.COVER, "same-url", "key");
            given(req.imageMetadataReqDTOList()).willReturn(List.of(meta));

            bookFacade.updateBook(bookId, req, Map.of());

            // imageCheck가 false여야 하므로 updateBookImage가 호출되지 않아야 함
            verify(imageService, times(0)).updateBookImage(any(), any(), any());
        }


        @Test
        @DisplayName("deleteBookById: 도서 없음 -> NotFoundBookId")
        void deleteBookById_notFound_throws() {
            when(bookCoreService.getBook_Id(5L)).thenReturn(null);
            assertThatThrownBy(() -> bookFacade.deleteBookById(5L)).isInstanceOf(NotFoundBookId.class);
        }

        @Test
        @DisplayName("deleteBookById: 정상 흐름 - 이미지 삭제, db 삭제, 퍼블리시")
        void deleteBookById_success() {
            Book book = new Book();
            book.setIsbn("X-1");
            when(bookCoreService.getBook_Id(10L)).thenReturn(book);
            when(bookCoreService.deleteBookByData(book)).thenReturn(book);

            bookFacade.deleteBookById(10L);

            verify(imageService).deleteBookImageOfBook(book);
            verify(bookCoreService).deleteBook(book);
            verify(bookSearchSyncPublisher).publishDeleteAfterCommit("X-1", "BOOK_DELETE");
        }

        @Test
        @DisplayName("deleteBookByIsbn: isbn -> id -> delete 호출")
        void deleteBookByIsbn_delegates() {
            when(bookCoreService.getBookIdByIsbn("S-1")).thenReturn(99L);

            Book book = new Book();
            book.setIsbn("S-1");
            when(bookCoreService.getBook_Id(99L)).thenReturn(book);

            when(bookCoreService.deleteBookByData(book)).thenReturn(book);

            bookFacade.deleteBookByIsbn("S-1");

            verify(bookCoreService).getBookIdByIsbn("S-1");
            verify(bookCoreService).getBook_Id(99L);
            verify(imageService).deleteBookImageOfBook(book);
            verify(bookCoreService).deleteBook(book);
            verify(bookSearchSyncPublisher).publishDeleteAfterCommit("S-1", "BOOK_DELETE");
        }


        @Test
        @DisplayName("getBookDetail: 할인 매핑 실패 -> FailObjectMapper")
        void getBookDetail_discountMappingFail_throws() throws JsonProcessingException {
            when(bookCoreService.getBookDetail_Id(2L)).thenReturn(mock(BookDetailProjection.class));

            when(discountPolicyService.getDiscountPrice(anyLong(), anyLong()))
                    .thenThrow(new JsonProcessingException("bad") {});

            assertThatThrownBy(() -> bookFacade.getBookDetail(2L, 1L))
                    .isInstanceOf(FailObjectMapper.class);
        }


        @Test
        @DisplayName("existIsbn: null 반환 -> NotFoundBookISBN")
        void existIsbn_null_throws() {
            doReturn(null).when(bookCoreService).existIsbn("A");

            assertThatThrownBy(() -> bookFacade.existIsbn("A"))
                    .isInstanceOf(NotFoundBookISBN.class);

            verify(bookCoreService).existIsbn("A");
        }


        @Test
        @DisplayName("getTotalDate: 정상 반환")
        void getTotalDate_success() {
            when(bookCoreService.getCountAll()).thenReturn(10L);
            when(bookCoreService.getCountByStatus(any())).thenReturn(2L);
            when(reviewService.getCountByRelease(7)).thenReturn(3L);
            when(categoryService.getCountAll()).thenReturn(4L);

            var dto = bookFacade.getTotalDate();
            assertThat(dto).isNotNull();
            assertThat(dto.totalCount()).isEqualTo(10L);
        }

        @Test
        @DisplayName("registerReview: 사진 있는 리뷰 -> REVIEW_PHOTO")
        void registerReview_withPhoto_returnsReviewPhoto() {
            ReviewReqDTO req = mock(ReviewReqDTO.class);
            when(req.bookId()).thenReturn(7L);
            Book book = new Book();
            when(bookCoreService.getBook_Id(7L)).thenReturn(book);
            Review review = mock(Review.class);
            when(review.getReviewImages()).thenReturn(List.of(mock(ReviewImage.class)));
            when(reviewService.registerReview(eq(req), anyMap(), eq(book))).thenReturn(review);

            PointPolicyType type = bookFacade.registerReview(req, Map.of());
            assertThat(type).isEqualTo(PointPolicyType.REVIEW_PHOTO);
        }

        // java
        @Test
        @DisplayName("getBookUpdateView: 정상 반환")
        void getBookUpdateView_success() throws JsonProcessingException {
            var detail = mock(BookUpdateViewProjection.class);
            when(bookCoreService.getBookUpdateView_Id(5L)).thenReturn(detail);

            var view = mock(BookUpdateView.class);
            when(bookMapper.toBookUpdateView(detail)).thenReturn(view);

            var result = bookFacade.getBookUpdateView(5L);
            assertThat(result).isSameAs(view);
            verify(bookCoreService).getBookUpdateView_Id(5L);
            verify(bookMapper).toBookUpdateView(detail);
        }

        @Test
        @DisplayName("getBookUpdateView: 매핑 실패 -> FailObjectMapper")
        void getBookUpdateView_mappingFail_throws() throws JsonProcessingException {
            var detail = mock(BookUpdateViewProjection.class);
            when(bookCoreService.getBookUpdateView_Id(6L)).thenReturn(detail);

            when(bookMapper.toBookUpdateView(detail)).thenThrow(new JsonProcessingException("bad") {});

            assertThatThrownBy(() -> bookFacade.getBookUpdateView(6L))
                    .isInstanceOf(FailObjectMapper.class);
            verify(bookCoreService).getBookUpdateView_Id(6L);
            verify(bookMapper).toBookUpdateView(detail);
        }

        @Test
        @DisplayName("getBookList: NEW_RELEASES 정상 흐름")
        void getBookList_newReleases_success() throws JsonProcessingException {
            // id list
            when(bookCoreService.getBookIdsOfNewReleases(any(), any(), eq(10))).thenReturn(List.of(11L));

            // projections
            var proj = mock(BookListProjection.class);
            when(proj.getId()).thenReturn(11L); // <- 실제 호출되는 id를 맞춰줌
            when(bookCoreService.getBookByIds(List.of(11L), false)).thenReturn(List.of(proj));

            // mapper -> data map
            Map<Long, BookListData> dataMap =
                    Map.of(11L, mock(BookListData.class));
            when(bookMapper.toBookListDataMap(List.of(proj))).thenReturn(dataMap);

            // like set (인자 매처를 완화하여 strict stubbing 회피)
            when(likeService.getLikeByUserIdAndBookIds(eq(1L), anyList())).thenReturn(Set.of(11L));

            // discount mapping
            Map<Long, DiscountDTO.Request> dtoMap = Map.of();
            when(bookMapper.toDiscountDTOMapByBookListData(dataMap)).thenReturn(dtoMap);
            Map<Long, DiscountDTO.Response> discountMap = Map.of();
            when(discountPolicyService.getDiscountPriceMap(dtoMap)).thenReturn(discountMap);

            // final DTO list
            var resp = mock(BookListRespDTO.class);
            when(bookMapper.toBookRespDTOList(dataMap, discountMap, Set.of(11L))).thenReturn(List.of(resp));

            var result = bookFacade.getBookList(null, BookListType.NEW_RELEASES, 1L);
            assertThat(result).isNotNull();
            assertThat(result.bookListRespDTOS()).hasSize(1);
        }


        @Test
        @DisplayName("findBooksByIdIn: 정상 흐름")
        void findBooksByIdIn_success() throws JsonProcessingException {
            var proj = mock(BookInfoListProjection.class);
            when(bookCoreService.getBookInfoListByInd(List.of(2L), false)).thenReturn(List.of(proj));

            Map<Long, DiscountDTO.Request> dtoMap = Map.of();
            when(bookMapper.toDiscountDTOMapByBookInfoListProjection(List.of(proj))).thenReturn(dtoMap);

            Map<Long, DiscountDTO.Response> discountMap = Map.of();
            when(discountPolicyService.getDiscountPriceMap(dtoMap)).thenReturn(discountMap);

            var resp = mock(OrderBooksInfoRespDTO.class);
            when(bookMapper.toOrderBookInfoRespDTOList(discountMap, List.of(proj))).thenReturn(resp);

            var result = bookFacade.findBooksByIdIn(List.of(2L));
            assertThat(result).isSameAs(resp);
        }

        @Test
        @DisplayName("getOrderBookList: 정상 흐름")
        void getOrderBookList_success() {
            var summ = mock(BookSummeryProjection.class);
            when(bookCoreService.getBookSummeryByIds(List.of(3L))).thenReturn(List.of(summ));

            var dto = mock(OrderBookSummeryDTO.class);
            when(bookMapper.toOrderBookSummeryDTOList(List.of(summ))).thenReturn(List.of(dto));

            var result = bookFacade.getOrderBookList(List.of(3L));
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isSameAs(dto);
        }

        @Test
        @DisplayName("findAllForAdmin: 정상 흐름")
        void findAllForAdmin_success() throws JsonProcessingException {
            var pageable = mock(org.springframework.data.domain.Pageable.class);
            var adminPage = mock(org.springframework.data.domain.Page.class);
            when(bookCoreService.getBookAdminProjectionPage(pageable)).thenReturn(adminPage);

            Map<Long, DiscountDTO.Request> dtoMap = Map.of();
            when(bookMapper.toDiscountDTOMapByBookAdminProjection(adminPage)).thenReturn(dtoMap);

            Map<Long, DiscountDTO.Response> discountMap = Map.of();
            when(discountPolicyService.getDiscountPriceMap(dtoMap)).thenReturn(discountMap);

            var respPage = mock(org.springframework.data.domain.Page.class);
            when(bookMapper.toBookAdminResopnseDTOPage(adminPage, discountMap)).thenReturn(respPage);

            var result = bookFacade.findAllForAdmin(pageable);
            assertThat(result).isSameAs(respPage);
            verify(bookCoreService).getBookAdminProjectionPage(pageable);
        }

        @Test
        @DisplayName("findAllForAdmin: 할인 매핑 실패 -> FailObjectMapper")
        void findAllForAdmin_discountMappingFail_throws() throws JsonProcessingException {
            var pageable = mock(org.springframework.data.domain.Pageable.class);
            var adminPage = mock(org.springframework.data.domain.Page.class);
            when(bookCoreService.getBookAdminProjectionPage(pageable)).thenReturn(adminPage);

            Map<Long, DiscountDTO.Request> dtoMap = Map.of();
            when(bookMapper.toDiscountDTOMapByBookAdminProjection(adminPage)).thenReturn(dtoMap);

            when(discountPolicyService.getDiscountPriceMap(dtoMap)).thenThrow(new JsonProcessingException("bad") {});

            assertThatThrownBy(() -> bookFacade.findAllForAdmin(pageable))
                    .isInstanceOf(FailObjectMapper.class);
            verify(bookCoreService).getBookAdminProjectionPage(pageable);
            verify(bookMapper).toDiscountDTOMapByBookAdminProjection(adminPage);
        }

        @Test
        @DisplayName("getBookList: 조회된 ID가 없을 때 (bookIds == null) -> NotFoundBook 예외")
        void getBookList_idsNull_throws() {
            when(bookCoreService.getBookIdsOfNewReleases(any(), any(), anyInt())).thenReturn(null);

            assertThatThrownBy(() -> bookFacade.getBookList(null, BookListType.NEW_RELEASES, 1L))
                    .isInstanceOf(NotFoundBook.class);
        }

        @Test
        @DisplayName("getBookDetail: Mapper에서 JsonProcessingException 발생 시 FailObjectMapper 처리")
        void getBookDetail_mapperException_throws() throws JsonProcessingException {
            var detail = mock(BookDetailProjection.class);
            when(bookCoreService.getBookDetail_Id(1L)).thenReturn(detail);
            when(discountPolicyService.getDiscountPrice(anyLong(), anyLong())).thenReturn(5000L);

            // mapper에서 강제로 예외 발생
            when(bookMapper.toBookRespDTO(any(), any(), any(), any())).thenThrow(new JsonProcessingException("error") {});

            assertThatThrownBy(() -> bookFacade.getBookDetail(1L, 1L))
                    .isInstanceOf(FailObjectMapper.class);
        }

        @Test
        @DisplayName("orderCancel: 양수/음수/제로 수량 및 품절 전환 모든 케이스")
        void orderCancel_allBranches_test() {
            // 케이스 1: 수량 > 0 (재고 복구 및 품절 해제)
            Book book1 = new Book(); book1.setStock(0); book1.setStatus(Status.SOLD_OUT);
            given(bookCoreService.getBook_Id(1L)).willReturn(book1);
            bookFacade.orderCancel(new OrderCancelRequest(1L, 5));
            assertThat(book1.getStatus()).isEqualTo(Status.ON_SALE);

            // 케이스 2: 수량 < 0 (재고 차감 및 품절 전환)
            Book book2 = new Book(); book2.setStock(10); book2.setStatus(Status.ON_SALE);
            given(bookCoreService.getBook_Id(2L)).willReturn(book2);
            bookFacade.orderCancel(new OrderCancelRequest(2L, -10));
            assertThat(book2.getStatus()).isEqualTo(Status.SOLD_OUT);

            // 케이스 3: 수량 < 0 이지만 재고 부족 (예외 발생)
            Book book3 = new Book(); book3.setStock(5);
            given(bookCoreService.getBook_Id(3L)).willReturn(book3);
            OrderCancelRequest orderCancelRequest = new OrderCancelRequest(3L, -10);
            assertThatThrownBy(() -> bookFacade.orderCancel(orderCancelRequest))
                    .isInstanceOf(DuplicatedBook.class);

            // 케이스 4: 수량 == 0 (변경 없음, 로그 분기)
            Book book4 = new Book(); book4.setStock(10);
            given(bookCoreService.getBook_Id(4L)).willReturn(book4);
            bookFacade.orderCancel(new OrderCancelRequest(4L, 0));
            assertThat(book4.getStock()).isEqualTo(10);
        }

    }

    @Nested
    @SpringJUnitConfig(classes = SpringTests.TestConfig.class)
    @DisplayName("Spring tests with @MockitoBean")
    class SpringTests {

        @MockitoBean BookCoreService bookCoreService;
        @MockitoBean BookImageServiceImpl imageService;
        @MockitoBean LikeService likeService;
        @MockitoBean DiscountPolicyService discountPolicyService;
        @MockitoBean ReviewService reviewService;
        @MockitoBean CategoryV2Service categoryService;
        @MockitoBean BookMapper bookMapper;
        @MockitoBean ImageMapper imageMapper;
        @MockitoBean BookSearchSyncPublisher bookSearchSyncPublisher;

        @Resource BookFacade bookFacade;

        @Test
        @DisplayName("Spring + MockitoBean: bean 주입 및 간단 호출 확인")
        void springContext_injection_and_call() {
            when(bookCoreService.getCountAll()).thenReturn(1L);
            when(bookCoreService.getCountByStatus(any())).thenReturn(0L);
            when(reviewService.getCountByRelease(7)).thenReturn(0L);
            when(categoryService.getCountAll()).thenReturn(0L);

            var dto = bookFacade.getTotalDate();
            assertThat(dto).isNotNull();
            verify(bookCoreService).getCountAll();
        }

        @Configuration
        static class TestConfig {
            @Bean
            BookFacade bookFacade(BookCoreService bookCoreService,
                                  BookImageServiceImpl imageService,
                                  LikeService likeService,
                                  DiscountPolicyService discountPolicyService,
                                  ReviewService reviewService,
                                  CategoryV2Service categoryService,
                                  BookMapper bookMapper,
                                  ImageMapper imageMapper,
                                  BookSearchSyncPublisher bookSearchSyncPublisher) {
                return new BookFacade(bookCoreService, imageService, likeService, discountPolicyService,
                        reviewService, categoryService, bookMapper, imageMapper, bookSearchSyncPublisher);
            }
        }
    }
}