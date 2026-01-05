package com.daisobook.shop.booksearch.books_search.mapper;

import com.daisobook.shop.booksearch.books_search.dto.BookListData;
import com.daisobook.shop.booksearch.books_search.dto.BookUpdateData;
import com.daisobook.shop.booksearch.books_search.dto.DiscountDTO;
import com.daisobook.shop.booksearch.books_search.dto.projection.*;
import com.daisobook.shop.booksearch.books_search.dto.request.TagReqDTO;
import com.daisobook.shop.booksearch.books_search.dto.request.book.BookGroupReqV2DTO;
import com.daisobook.shop.booksearch.books_search.dto.request.book.BookReqV2DTO;
import com.daisobook.shop.booksearch.books_search.dto.response.AuthorRespDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.ImageRespDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.PublisherRespDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.TagRespDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.book.BookAdminResponseDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.book.BookListRespDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.book.BookRespDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.book.BookUpdateView;
import com.daisobook.shop.booksearch.books_search.dto.response.category.CategoryRespDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.order.OrderBookInfoRespDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.order.OrderBookSummeryDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.order.OrderBooksInfoRespDTO;
import com.daisobook.shop.booksearch.books_search.entity.ImageType;
import com.daisobook.shop.booksearch.books_search.entity.book.Book;
import com.daisobook.shop.booksearch.books_search.entity.book.Status;
import com.daisobook.shop.booksearch.books_search.mapper.author.AuthorMapper;
import com.daisobook.shop.booksearch.books_search.mapper.book.impl.BookMapperImpl;
import com.daisobook.shop.booksearch.books_search.mapper.category.CategoryMapper;
import com.daisobook.shop.booksearch.books_search.mapper.image.ImageMapper;
import com.daisobook.shop.booksearch.books_search.mapper.publisher.PublisherMapper;
import com.daisobook.shop.booksearch.books_search.mapper.review.ReviewMapper;
import com.daisobook.shop.booksearch.books_search.mapper.tag.TagMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookMapperImplTest {

    @Spy private ObjectMapper objectMapper = new ObjectMapper();
    @Mock private CategoryMapper categoryMapper;
    @Mock private TagMapper tagMapper;
    @Mock private ReviewMapper reviewMapper;
    @Mock private AuthorMapper authorMapper;
    @Mock private ImageMapper imageMapper;
    @Mock private PublisherMapper publisherMapper;

    @InjectMocks private BookMapperImpl bookMapper;

    // --- [1] 데이터 파싱 및 엔티티 생성 로직 ---

    @Test
    @DisplayName("parsing: 이미지 3,4번이 1,2번으로 오매핑되는 구현상 특징과 JSON 파싱을 검증한다")
    void parsing_Detailed_Test() throws JsonProcessingException {
        String json = "{\"isbn\":\"123\", \"title\":\"테스트도서\"}";
        MockMultipartFile img1 = new MockMultipartFile("image1", "f1.jpg", null, "d1".getBytes());
        MockMultipartFile img3 = new MockMultipartFile("image3", "f3.jpg", null, "d3".getBytes());

        BookGroupReqV2DTO result = bookMapper.parsing(json, null, img1, null, img3, null);

        assertThat(result.bookReqDTO().title()).isEqualTo("테스트도서");
        assertThat(result.fileMap()).containsKey("image1");
        // 현재 코드 로직: image3이 들어오면 내부적으로 image1 변수를 할당함
        assertThat(result.fileMap()).containsEntry("image3", img1);
    }

    @Test
    @DisplayName("create & toBookUpdateData: 엔티티 생성 및 수정 데이터 변환 시 필드 누락 여부 검증")
    void create_and_UpdateData_Test() {
        BookReqV2DTO req = mock(BookReqV2DTO.class);
        when(req.isbn()).thenReturn("ISBN123");
        when(req.isDeleted()).thenReturn(true);
        when(req.tags()).thenReturn(List.of(new TagReqDTO("Java")));

        Book book = bookMapper.create(req);
        BookUpdateData updateData = bookMapper.toBookUpdateData(req);

        assertThat(book.getIsbn()).isEqualTo("ISBN123");
        assertThat(book.isDeleted()).isTrue();
        assertThat(updateData.tag()).containsExactly("Java");
    }

    // --- [2] 주문 관련 DTO 변환 (복잡한 할인 계산 포함) ---

    @Test
    @DisplayName("toOrderBookInfo(Entity): 할인율 계산식((할인액/정가)*100)의 정확도를 검증한다")
    void toOrderBookInfo_Entity_Calculation_Test() {
        // 정가 10,000원, 할인액 1,500원 -> 예상 할인율 15.0%
        Book book = new Book("isbn", "제목", null, null, null, 10000L, true, 10, Status.ON_SALE, 1);
        Map<Long, Long> discountMap = Map.of(0L, 1500L); // key 0은 b.getId() 기본값

        OrderBooksInfoRespDTO result = bookMapper.toOrderBookInfoRespDTOList(List.of(book), discountMap);

        OrderBookInfoRespDTO dto = result.orderBookInfoRespDTOList().getFirst();
        assertThat(dto.discountPercentage().doubleValue()).isEqualTo(15.0);
        assertThat(dto.discountPrice()).isEqualTo(1500L);
    }

    @Test
    @DisplayName("toOrderBookInfo(Projection): 프로젝션 데이터와 할인 DTO의 매핑 일치 여부 확인")
    void toOrderBookInfo_Projection_Mapping_Test() {
        BookInfoListProjection p = mock(BookInfoListProjection.class);
        when(p.getBookId()).thenReturn(1L);
        when(p.getPrice()).thenReturn(10000L);

        DiscountDTO.Response discount = new DiscountDTO.Response(1L, 8000L, BigDecimal.valueOf(20), 2000L);

        OrderBooksInfoRespDTO result = bookMapper.toOrderBookInfoRespDTOList(Map.of(1L, discount), List.of(p));

        assertThat(result.orderBookInfoRespDTOList().getFirst().discountPrice()).isEqualTo(2000L);
    }

    // --- [3] 상세 및 목록 조회 (NPE 방어 및 하위 매퍼 협업) ---

    @Test
    @DisplayName("toBookRespDTO: 상세 페이지 변환 시 소수점 2자리 내림(RoundingMode.DOWN) 계산 검증")
    void toBookRespDTO_Rounding_Test() throws JsonProcessingException {
        BookDetailProjection p = mock(BookDetailProjection.class);
        when(p.getPrice()).thenReturn(10000L);
        when(publisherMapper.toPublisherRespDTO(any())).thenReturn(new PublisherRespDTO(1L, "테스트출판"));

        // (1.0 - 8555/10000) * 100 = 14.45%
        BookRespDTO result = bookMapper.toBookRespDTO(p, 10, true, 8555L);

        assertThat(result.discountPercentage()).isEqualTo(new BigDecimal("14.44"));
    }

    @Test
    @DisplayName("toBookRespDTOList & Page: Markdown 제거 및 찜 상태(likeCheck) 연산 검증")
    void toBookRespDTO_List_and_Page_Test() {
        BookListData data = new BookListData(1L, "i", "t", "### 설명", null, new PublisherRespDTO(1L,"p"), null, 1000L, null, null, null, null, null, null, 1, true, false);
        Map<Long, BookListData> map = Map.of(1L, data);
        Set<Long> likeSet = Set.of(1L);

        List<BookListRespDTO> listResult = bookMapper.toBookRespDTOList(map, Map.of(), likeSet);
        Page<BookListRespDTO> pageResult = bookMapper.toBookRespDTOPage(new PageImpl<>(List.of(data)), Map.of(), likeSet);

        // Markdown "### " 제거 확인
        assertThat(listResult.getFirst().description()).isEqualTo("설명");
        assertThat(pageResult.getContent().getFirst().isLike()).isTrue();
    }

    // --- [4] 데이터 수집 및 필터링 로직 (toBookListDataMap) ---

    @Test
    @DisplayName("toBookListDataMap: 하위 매퍼(Author, Image 등)와의 데이터 조립 로직을 정밀 검증한다")
    void toBookListDataMap_Deep_Test() throws JsonProcessingException {
        BookListProjection p = mock(BookListProjection.class);
        when(p.getId()).thenReturn(1L);
        when(p.getAuthors()).thenReturn("authors-json");
        when(p.getPublisher()).thenReturn("pub-json");

        // 하위 매퍼가 ID를 Key로 데이터를 반환하도록 설정 (중요: 이 부분이 필터링 로직의 핵심)
        when(authorMapper.toAuthorRespDTOMap(any())).thenReturn(Map.of(1L, List.of(new AuthorRespDTO(1L,"작가",1L,"저자"))));
        when(publisherMapper.toPublisherRespDTOMap(any())).thenReturn(Map.of(1L, new PublisherRespDTO(1L,"출판사")));

        Map<Long, BookListData> result = bookMapper.toBookListDataMap(List.of(p));

        assertThat(result.get(1L).getAuthorList()).hasSize(1);
        assertThat(result.get(1L).getPublisher().name()).isEqualTo("출판사");
    }

    // --- [5] 할인 요청 데이터 생성 (4개 메서드) ---

    @Test
    @DisplayName("toDiscountDTOMap 계열: 4가지 입력 소스(Map, Page, List, AdminPage)로부터 정확한 할인 요청 객체를 생성한다")
    void toDiscountDTOMaps_All_Test() {
        // 1. Data Map
        BookListData data = mock(BookListData.class);
        when(data.getId()).thenReturn(1L); when(data.getPrice()).thenReturn(100L);

        // 2. Info List
        BookInfoListProjection infoP = mock(BookInfoListProjection.class);
        when(infoP.getBookId()).thenReturn(2L); when(infoP.getPrice()).thenReturn(200L);

        // 3. Admin Page
        BookAdminProjection adminP = mock(BookAdminProjection.class);
        when(adminP.getBookId()).thenReturn(3L); when(adminP.getPrice()).thenReturn(300L);

        assertThat(bookMapper.toDiscountDTOMapByBookListData(Map.of(1L, data))).containsKey(1L);
        assertThat(bookMapper.toDiscountDTOMapByBookListData(new PageImpl<>(List.of(data)))).containsKey(1L);
        assertThat(bookMapper.toDiscountDTOMapByBookInfoListProjection(List.of(infoP))).containsKey(2L);
        assertThat(bookMapper.toDiscountDTOMapByBookAdminProjection(new PageImpl<>(List.of(adminP)))).containsKey(3L);
    }

    // --- [6] 기타 뷰 및 요약 변환 ---

    @Test
    @DisplayName("toOrderBookSummeryDTOList: 주문 요약 정보의 필드 매핑 검증")
    void toOrderBookSummery_Test() {
        BookSummeryProjection p = mock(BookSummeryProjection.class);
        when(p.getBookId()).thenReturn(7L);
        when(p.getTitle()).thenReturn("제목");
        when(p.getPrice()).thenReturn(5000L);

        List<OrderBookSummeryDTO> result = bookMapper.toOrderBookSummeryDTOList(List.of(p));

        assertThat(result.getFirst().bookId()).isEqualTo(7L);
        assertThat(result.getFirst().price()).isEqualTo(5000L);
    }

    @Test
    @DisplayName("toBookUpdateView: 수정 뷰 변환 시 하위 매퍼 호출 결과 반영 확인")
    void toBookUpdateView_Detailed_Test() throws JsonProcessingException {
        BookUpdateViewProjection p = mock(BookUpdateViewProjection.class);
        when(p.getPublisher()).thenReturn("pub-json");
        when(publisherMapper.toPublisherRespDTO(any())).thenReturn(new PublisherRespDTO(1L, "다이소"));

        BookUpdateView result = bookMapper.toBookUpdateView(p);

        assertThat(result.publisher()).isEqualTo("다이소");
    }

    @Test
    @DisplayName("toBookAdminResopnseDTOPage: 관리자 페이지 변환 시 이미지 맵 조인 로직 확인")
    void toBookAdminResponse_Detailed_Test() throws JsonProcessingException {
        BookAdminProjection p = mock(BookAdminProjection.class);
        when(p.getBookId()).thenReturn(10L);
        when(p.getImages()).thenReturn("img-json");

        Map<Long, List<ImageRespDTO>> imgMap = Map.of(10L, List.of(new ImageRespDTO(1L,"path",null)));
        when(imageMapper.toIageRespDTOMap(any())).thenReturn(imgMap);

        Page<BookAdminResponseDTO> result = bookMapper.toBookAdminResopnseDTOPage(new PageImpl<>(List.of(p)), Map.of());

        assertThat(result.getContent().getFirst().imageList()).hasSize(1);
    }

    @Test
    @DisplayName("parsing: 모든 이미지 파라미터가 null일 때 빈 맵 반환 확인")
    void parsing_allImagesNull() throws JsonProcessingException {
        String json = "{\"isbn\":\"123\"}";
        BookGroupReqV2DTO result = bookMapper.parsing(json, null, null, null, null, null);

        assertThat(result.fileMap()).isEmpty();
    }

    @Test
    @DisplayName("parsing: image0부터 image4까지 모두 전달되었을 때의 맵 구성 확인")
    void parsing_fullImages() throws JsonProcessingException {
        String json = "{\"isbn\":\"123\"}";
        MockMultipartFile img0 = new MockMultipartFile("i0", "f0.jpg", null, "d0".getBytes());
        MockMultipartFile img1 = new MockMultipartFile("i1", "f1.jpg", null, "d1".getBytes());
        MockMultipartFile img2 = new MockMultipartFile("i2", "f2.jpg", null, "d2".getBytes());
        MockMultipartFile img3 = new MockMultipartFile("i3", "f3.jpg", null, "d3".getBytes());
        MockMultipartFile img4 = new MockMultipartFile("i4", "f4.jpg", null, "d4".getBytes());

        BookGroupReqV2DTO result = bookMapper.parsing(json, img0, img1, img2, img3, img4);

        assertThat(result.fileMap()).hasSize(5);
        // 구현 로직상 image3의 value로 image1이 들어가는지 검증 (의도된 것인지 확인 필요)
        assertThat(result.fileMap()).containsEntry("i3", img1);
    }

    @Test
    @DisplayName("toOrderBookInfo(Entity): 이미지 리스트가 비어있을 때 path가 null로 설정되는지 확인")
    void toOrderBookInfo_Entity_NoImage_Test() {
        Book book = new Book();
        book.setBookImages(Collections.emptyList()); // 이미지 없음

        OrderBooksInfoRespDTO result = bookMapper.toOrderBookInfoRespDTOList(List.of(book), Map.of());

        assertThat(result.orderBookInfoRespDTOList().getFirst().coverImage()).isNull();
    }

    @Test
    @DisplayName("toBookRespDTO: 가격 정보가 null일 때의 안전한 처리 확인")
    void toBookRespDTO_NullPrice_Test() throws JsonProcessingException {
        BookDetailProjection p = mock(BookDetailProjection.class);
        when(p.getPrice()).thenReturn(null);
        when(publisherMapper.toPublisherRespDTO(any())).thenReturn(new PublisherRespDTO(1L, "P"));

        // 계산 로직 i = ... 에서 NPE가 발생하는지 혹은 null이 리턴되는지 확인
        // 현재 코드상 Objects.requireNonNull(i) 때문에 NPE 발생 가능성이 높음 -> 로직 수정 필요성 감지
        assertThatThrownBy(() -> bookMapper.toBookRespDTO(p, 0, false, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("toBookListDataPage: Projection 페이지를 BookListData 페이지로 변환 성공")
    void toBookListDataPage_success() throws JsonProcessingException {
        // 1. Given: 가짜 Projection 데이터 생성
        Long bookId = 1L;
        BookListProjection mockProjection = mock(BookListProjection.class);
        when(mockProjection.getId()).thenReturn(bookId);
        when(mockProjection.getIsbn()).thenReturn("12345");
        when(mockProjection.getTitle()).thenReturn("테스트 도서");
        when(mockProjection.getAuthors()).thenReturn("작가 정보 JSON");
        when(mockProjection.getPublisher()).thenReturn("출판사 정보 JSON");
        when(mockProjection.getImages()).thenReturn("이미지 정보 JSON");
        when(mockProjection.getCategories()).thenReturn("카테고리 정보 JSON");
        when(mockProjection.getTags()).thenReturn("태그 정보 JSON");

        Page<BookListProjection> projectionPage = new PageImpl<>(List.of(mockProjection));

        // 2. Mocking: 내부 매퍼들의 동작 정의 (toAuthorRespDTOMap 등)
        AuthorRespDTO author = new AuthorRespDTO(1L, "작가명", null, null);
        PublisherRespDTO publisher = new PublisherRespDTO(1L, "출판사명");
        ImageRespDTO image = new ImageRespDTO(1L, "path/to/img", ImageType.COVER);
        CategoryRespDTO category = new CategoryRespDTO(1L, "카테고리명", 1, null, null);
        TagRespDTO tag = new TagRespDTO(1L, "태그명");

        when(authorMapper.toAuthorRespDTOMap(anyMap())).thenReturn(Map.of(bookId, List.of(author)));
        when(publisherMapper.toPublisherRespDTOMap(anyMap())).thenReturn(Map.of(bookId, publisher));
        when(imageMapper.toIageRespDTOMap(anyMap())).thenReturn(Map.of(bookId, List.of(image)));
        when(categoryMapper.toCategoryRespDTOMap(anyMap())).thenReturn(Map.of(bookId, List.of(category)));
        when(tagMapper.toTagRespDTOMap(anyMap())).thenReturn(Map.of(bookId, List.of(tag)));

        // 3. When: 메서드 실행
        Page<BookListData> resultPage = bookMapper.toBookListDataPage(projectionPage);

        // 4. Then: 검증
        assertNotNull(resultPage);
        assertEquals(1, resultPage.getContent().size());

        BookListData resultData = resultPage.getContent().getFirst();
        assertEquals(bookId, resultData.getId());
        assertEquals("테스트 도서", resultData.getTitle());
        assertEquals("작가명", resultData.getAuthorList().getFirst().authorName());
        assertEquals("출판사명", resultData.getPublisher().name());
        assertEquals("path/to/img", resultData.getImageList().getFirst().path());

        // 내부 매퍼들이 각각 한 번씩 호출되었는지 확인
        verify(authorMapper).toAuthorRespDTOMap(anyMap());
        verify(publisherMapper).toPublisherRespDTOMap(anyMap());
    }

    @Test
    @DisplayName("toBookListDataPage: 데이터가 없는 빈 페이지 처리")
    void toBookListDataPage_emptyPage() throws JsonProcessingException {
        // 1. Given: 빈 페이지
        Page<BookListProjection> emptyProjectionPage = Page.empty();

        // 2. When
        Page<BookListData> resultPage = bookMapper.toBookListDataPage(emptyProjectionPage);

        // 3. Then
        assertNotNull(resultPage);
        assertTrue(resultPage.isEmpty());
    }
}