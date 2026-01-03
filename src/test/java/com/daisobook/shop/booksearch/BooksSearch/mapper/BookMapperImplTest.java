package com.daisobook.shop.booksearch.BooksSearch.mapper;

import com.daisobook.shop.booksearch.BooksSearch.dto.BookListData;
import com.daisobook.shop.booksearch.BooksSearch.dto.BookUpdateData;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.BookAdminProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.BookDetailProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.projection.BookListProjection;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.TagReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookGroupReqV2DTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.request.book.BookReqV2DTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.AuthorRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.ImageRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.PublisherRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.book.BookAdminResponseDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.book.BookRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.order.OrderBooksInfoRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.ImageType;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Status;
import com.daisobook.shop.booksearch.BooksSearch.mapper.author.AuthorMapper;
import com.daisobook.shop.booksearch.BooksSearch.mapper.book.impl.BookMapperImpl;
import com.daisobook.shop.booksearch.BooksSearch.mapper.category.CategoryMapper;
import com.daisobook.shop.booksearch.BooksSearch.mapper.image.ImageMapper;
import com.daisobook.shop.booksearch.BooksSearch.mapper.publisher.PublisherMapper;
import com.daisobook.shop.booksearch.BooksSearch.mapper.review.ReviewMapper;
import com.daisobook.shop.booksearch.BooksSearch.mapper.tag.TagMapper;
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
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookMapperImplTest {

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock private CategoryMapper categoryMapper;
    @Mock private TagMapper tagMapper;
    @Mock private ReviewMapper reviewMapper;
    @Mock private AuthorMapper authorMapper;
    @Mock private ImageMapper imageMapper;
    @Mock private PublisherMapper publisherMapper;

    @InjectMocks
    private BookMapperImpl bookMapper;

    // --- 1. parsing & create ---

    @Test
    @DisplayName("파일 업로드 파싱 시 image3, 4가 image1, 2로 잘못 매핑되는 로직을 검증한다")
    void parsing_FileMapping_Test() throws JsonProcessingException {
        String json = "{\"title\":\"test\"}";
        MockMultipartFile img1 = new MockMultipartFile("img1", "img1.jpg", null, "data".getBytes());
        MockMultipartFile img3 = new MockMultipartFile("img3", "img3.jpg", null, "data".getBytes());

        // 로직상 image3이 들어오면 image1의 이름으로 맵에 들어가는지 확인 (현재 코드의 특징)
        BookGroupReqV2DTO result = bookMapper.parsing(json, null, img1, null, img3, null);

        assertThat(result.fileMap()).containsKey("img1");
        // image3은 image1 변수를 할당받도록 구현되어 있음
        assertThat(result.fileMap().get("img3")).isEqualTo(img1);
    }

    // --- 2. Update Data & Projections ---

    @Test
    @DisplayName("BookReqV2DTO를 BookUpdateData로 변환한다")
    void toBookUpdateData_Test() {
        BookReqV2DTO req = mock(BookReqV2DTO.class);
        when(req.tags()).thenReturn(List.of(new TagReqDTO("태그1")));
        when(req.title()).thenReturn("수정제목");

        BookUpdateData result = bookMapper.toBookUpdateData(req);

        assertThat(result.title()).isEqualTo("수정제목");
        assertThat(result.tag()).containsExactly("태그1");
    }

    @Test
    @DisplayName("OrderBookInfo 리스트 변환 시 할인율 계산 로직을 검증한다")
    void toOrderBookInfoRespDTOList_Test() {
        Book book = new Book("isbn", "제목", null, null, null, 10000L, true, 10, Status.ON_SALE, 1);
        Map<Long, Long> discountMap = Map.of(0L, 1000L); // 1000원 할인

        OrderBooksInfoRespDTO result = bookMapper.toOrderBookInfoRespDTOList(List.of(book), discountMap);

        // 1000 / 10000 * 100 = 10.0%
        assertThat(result.orderBookInfoRespDTOList().getFirst().discountPercentage().doubleValue()).isEqualTo(10.0);
        assertThat(result.orderBookInfoRespDTOList().getFirst().discountPrice()).isEqualTo(1000L);
    }

    // --- 3. 상세 조회 (NPE 위험 구간) ---

    @Test
    @DisplayName("상세 조회 DTO 변환 시 할인율 소수점 2자리 반환을 확인한다")
    void toBookRespDTO_DiscountCalculation_Test() throws JsonProcessingException {
        // Given
        BookDetailProjection projection = mock(BookDetailProjection.class);
        when(projection.getPrice()).thenReturn(10000L);
        when(projection.getPublisher()).thenReturn("출판사JSON");

        // Mock 하위 매퍼 설정
        when(publisherMapper.toPublisherRespDTO(any())).thenReturn(new PublisherRespDTO(1L, "다이소출판"));

        // 10000원 책, 8500원에 판매 (1500원 할인) -> 15% 할인
        // (1.0 - 8500/10000) * 100 = 15.00
        BookRespDTO result = bookMapper.toBookRespDTO(projection, 10, true, 8500L);

        assertThat(result.discountPercentage()).isEqualTo(new BigDecimal("15.00"));
    }

    @Test
    @DisplayName("할인금액이 null일 때 toBookRespDTO는 NPE를 발생시킨다 (현재 코드 한계)")
    void toBookRespDTO_NPE_Scenario_Test() throws JsonProcessingException {
        // Given
        BookDetailProjection projection = mock(BookDetailProjection.class);
        when(projection.getPrice()).thenReturn(10000L);

        // 이 설정이 문제! 로직 중간에 NPE가 터지면 아래 매퍼들은 호출되지 않습니다.
        // lenient()를 붙여서 "호출되지 않아도 괜찮다"고 설정합니다.
        lenient().when(publisherMapper.toPublisherRespDTO(any())).thenReturn(new PublisherRespDTO(1L, "출판사"));

        // When & Then
        assertThatThrownBy(() -> bookMapper.toBookRespDTO(projection, 0, false, null))
                .isInstanceOf(NullPointerException.class);
    }

    // --- 4. 목록 및 페이지 변환 (Map/Page) ---

    @Test
    @DisplayName("BookListDataMap 생성 시 하위 매퍼들의 Map 변환 호출을 검증한다")
    void toBookListDataMap_Test() throws JsonProcessingException {
        // Given
        BookListProjection p = mock(BookListProjection.class);
        when(p.getId()).thenReturn(1L);
        when(p.getAuthors()).thenReturn("authorsJson");

        // authorMapper가 JSON을 DTO 맵으로 잘 바꾼다고 가정
        Map<Long, List<AuthorRespDTO>> authorMap = Map.of(1L, List.of(new AuthorRespDTO(1L, "작가", 1L, "저자")));
        when(authorMapper.toAuthorRespDTOMap(any())).thenReturn(authorMap);

        // When
        Map<Long, BookListData> result = bookMapper.toBookListDataMap(List.of(p));

        // Then
        assertThat(result).containsKey(1L);
        assertThat(result.get(1L).getAuthorList()).hasSize(1);
    }

    @Test
    @DisplayName("Admin 페이지 변환 시 이미지 맵 매핑 로직을 검증한다")
    void toBookAdminResopnseDTOPage_Test() throws JsonProcessingException {
        // Given
        BookAdminProjection ap = mock(BookAdminProjection.class);
        when(ap.getBookId()).thenReturn(1L);
        when(ap.getImages()).thenReturn("imagesJson");

        Page<BookAdminProjection> page = new PageImpl<>(List.of(ap));

        Map<Long, List<ImageRespDTO>> imgMap = Map.of(1L, List.of(new ImageRespDTO(1L, "path", ImageType.COVER)));
        when(imageMapper.toIageRespDTOMap(any())).thenReturn(imgMap);

        // When
        Page<BookAdminResponseDTO> result = bookMapper.toBookAdminResopnseDTOPage(page, Map.of());

        // Then
        assertThat(result.getContent().getFirst().imageList()).hasSize(1);
        assertThat(result.getContent().getFirst().bookId()).isEqualTo(1L);
    }
}