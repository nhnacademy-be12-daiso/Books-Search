package com.daisobook.shop.booksearch.BooksSearch.service.book.impl;

import com.daisobook.shop.booksearch.BooksSearch.dto.BookUpdateData;
import com.daisobook.shop.booksearch.BooksSearch.dto.point.PointPolicyType;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.BookDetailProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.AuthorReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.ImageMetadataReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.TagReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookReqV2DTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookGroupReqV2DTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.review.ReviewReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.service.ImagesReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.BookImage;
import com.daisobook.shop.booksearch.BooksSearch.entity.review.Review;
import com.daisobook.shop.booksearch.BooksSearch.entity.review.ReviewImage;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.book.NotFoundBookId;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.book.NotFoundBookISBN;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.mapper.FailObjectMapper;
import com.daisobook.shop.booksearch.BooksSearch.mapper.book.BookMapper;
import com.daisobook.shop.booksearch.BooksSearch.mapper.image.ImageMapper;
import com.daisobook.shop.booksearch.BooksSearch.search.component.BookSearchSyncPublisher;
import com.daisobook.shop.booksearch.BooksSearch.service.category.CategoryV2Service;
import com.daisobook.shop.booksearch.BooksSearch.service.image.impl.BookImageServiceImpl;
import com.daisobook.shop.booksearch.BooksSearch.service.like.LikeService;
import com.daisobook.shop.booksearch.BooksSearch.service.policy.DiscountPolicyService;
import com.daisobook.shop.booksearch.BooksSearch.service.review.ReviewService;
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

import static org.assertj.core.api.Assertions.*;
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
        void parsing_nullMetadata_throws() {
            assertThatThrownBy(() -> bookFacade.parsing(null, null, null, null, null, null))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("metadata is null");
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
            var detail = mock(com.daisobook.shop.booksearch.BooksSearch.dto.projection.BookUpdateViewProjection.class);
            when(bookCoreService.getBookUpdateView_Id(5L)).thenReturn(detail);

            var view = mock(com.daisobook.shop.booksearch.BooksSearch.dto.response.book.BookUpdateView.class);
            when(bookMapper.toBookUpdateView(detail)).thenReturn(view);

            var result = bookFacade.getBookUpdateView(5L);
            assertThat(result).isSameAs(view);
            verify(bookCoreService).getBookUpdateView_Id(5L);
            verify(bookMapper).toBookUpdateView(detail);
        }

        @Test
        @DisplayName("getBookUpdateView: 매핑 실패 -> FailObjectMapper")
        void getBookUpdateView_mappingFail_throws() throws JsonProcessingException {
            var detail = mock(com.daisobook.shop.booksearch.BooksSearch.dto.projection.BookUpdateViewProjection.class);
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
            var proj = mock(com.daisobook.shop.booksearch.BooksSearch.dto.projection.BookListProjection.class);
            when(proj.getId()).thenReturn(11L); // <- 실제 호출되는 id를 맞춰줌
            when(bookCoreService.getBookByIds(List.of(11L), false)).thenReturn(List.of(proj));

            // mapper -> data map
            Map<Long, com.daisobook.shop.booksearch.BooksSearch.dto.BookListData> dataMap =
                    Map.of(11L, mock(com.daisobook.shop.booksearch.BooksSearch.dto.BookListData.class));
            when(bookMapper.toBookListDataMap(List.of(proj))).thenReturn(dataMap);

            // like set (인자 매처를 완화하여 strict stubbing 회피)
            when(likeService.getLikeByUserIdAndBookIds(eq(1L), anyList())).thenReturn(Set.of(11L));

            // discount mapping
            Map<Long, com.daisobook.shop.booksearch.BooksSearch.dto.DiscountDTO.Request> dtoMap = Map.of();
            when(bookMapper.toDiscountDTOMapByBookListData(dataMap)).thenReturn(dtoMap);
            Map<Long, com.daisobook.shop.booksearch.BooksSearch.dto.DiscountDTO.Response> discountMap = Map.of();
            when(discountPolicyService.getDiscountPriceMap(dtoMap)).thenReturn(discountMap);

            // final DTO list
            var resp = mock(com.daisobook.shop.booksearch.BooksSearch.dto.response.book.BookListRespDTO.class);
            when(bookMapper.toBookRespDTOList(dataMap, discountMap, Set.of(11L))).thenReturn(List.of(resp));

            var result = bookFacade.getBookList(null, com.daisobook.shop.booksearch.BooksSearch.entity.BookListType.NEW_RELEASES, 1L);
            assertThat(result).isNotNull();
            assertThat(result.bookListRespDTOS()).hasSize(1);
        }


        @Test
        @DisplayName("findBooksByIdIn: 정상 흐름")
        void findBooksByIdIn_success() throws JsonProcessingException {
            var proj = mock(com.daisobook.shop.booksearch.BooksSearch.dto.projection.BookInfoListProjection.class);
            when(bookCoreService.getBookInfoListByInd(List.of(2L), false)).thenReturn(List.of(proj));

            Map<Long, com.daisobook.shop.booksearch.BooksSearch.dto.DiscountDTO.Request> dtoMap = Map.of();
            when(bookMapper.toDiscountDTOMapByBookInfoListProjection(List.of(proj))).thenReturn(dtoMap);

            Map<Long, com.daisobook.shop.booksearch.BooksSearch.dto.DiscountDTO.Response> discountMap = Map.of();
            when(discountPolicyService.getDiscountPriceMap(dtoMap)).thenReturn(discountMap);

            var resp = mock(com.daisobook.shop.booksearch.BooksSearch.dto.response.order.OrderBooksInfoRespDTO.class);
            when(bookMapper.toOrderBookInfoRespDTOList(discountMap, List.of(proj))).thenReturn(resp);

            var result = bookFacade.findBooksByIdIn(List.of(2L));
            assertThat(result).isSameAs(resp);
        }

        @Test
        @DisplayName("getOrderBookList: 정상 흐름")
        void getOrderBookList_success() {
            var summ = mock(com.daisobook.shop.booksearch.BooksSearch.dto.projection.BookSummeryProjection.class);
            when(bookCoreService.getBookSummeryByIds(List.of(3L))).thenReturn(List.of(summ));

            var dto = mock(com.daisobook.shop.booksearch.BooksSearch.dto.response.order.OrderBookSummeryDTO.class);
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

            Map<Long, com.daisobook.shop.booksearch.BooksSearch.dto.DiscountDTO.Request> dtoMap = Map.of();
            when(bookMapper.toDiscountDTOMapByBookAdminProjection(adminPage)).thenReturn(dtoMap);

            Map<Long, com.daisobook.shop.booksearch.BooksSearch.dto.DiscountDTO.Response> discountMap = Map.of();
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

            Map<Long, com.daisobook.shop.booksearch.BooksSearch.dto.DiscountDTO.Request> dtoMap = Map.of();
            when(bookMapper.toDiscountDTOMapByBookAdminProjection(adminPage)).thenReturn(dtoMap);

            when(discountPolicyService.getDiscountPriceMap(dtoMap)).thenThrow(new JsonProcessingException("bad") {});

            assertThatThrownBy(() -> bookFacade.findAllForAdmin(pageable))
                    .isInstanceOf(FailObjectMapper.class);
            verify(bookCoreService).getBookAdminProjectionPage(pageable);
            verify(bookMapper).toDiscountDTOMapByBookAdminProjection(adminPage);
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
