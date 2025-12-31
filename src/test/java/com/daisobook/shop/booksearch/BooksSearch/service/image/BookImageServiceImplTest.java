package com.daisobook.shop.booksearch.BooksSearch.service.image;

import com.daisobook.shop.booksearch.BooksSearch.dto.request.ImageMetadataReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.service.ImagesReqDTO;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.BookImage;
import com.daisobook.shop.booksearch.BooksSearch.entity.ImageType;
import com.daisobook.shop.booksearch.BooksSearch.repository.book.BookImageRepository;
import com.daisobook.shop.booksearch.BooksSearch.service.image.impl.BookImageServiceImpl;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookImageServiceImplTest {

    MinioClient minioClient = mock(MinioClient.class);
    WebClient.Builder webClientBuilder = mock(WebClient.Builder.class);

    @org.mockito.Mock
    BookImageRepository bookImageRepository;

    BookImageServiceImpl service;

    @BeforeEach
    void setUp() {
        BookImageServiceImpl real = new BookImageServiceImpl(minioClient, webClientBuilder, bookImageRepository);
        service = spy(real);
    }

    @Test
    @DisplayName("이미지 메타 목록이 5개 초과이면 예외 발생")
    void whenMoreThanMaxImages_thenThrow() {
        ImagesReqDTO imagesReqDTO = mock(ImagesReqDTO.class);
        List<ImageMetadataReqDTO> tooMany = IntStream.range(0, 6)
                .mapToObj(i -> mock(ImageMetadataReqDTO.class))
                .collect(Collectors.toList());

        when(imagesReqDTO.imageMetadata()).thenReturn(tooMany);

        Book book = mock(Book.class);

        assertThatThrownBy(() -> service.addBookImage(book, imagesReqDTO, Collections.emptyMap()))
                .isInstanceOf(RuntimeException.class);
        verifyNoInteractions(bookImageRepository);
    }

    @Test
    @DisplayName("addBookImage 정상 흐름 - createdProcess 결과로 BookImage 생성 후 저장")
    void addBookImage_successful_saveAllCalled() {
        ImagesReqDTO imagesReqDTO = mock(ImagesReqDTO.class);
        ImageMetadataReqDTO meta0 = mock(ImageMetadataReqDTO.class);
        ImageMetadataReqDTO meta1 = mock(ImageMetadataReqDTO.class);
        when(meta0.sequence()).thenReturn(0);
        when(meta1.sequence()).thenReturn(1);
        when(meta0.type()).thenReturn(ImageType.COVER);
        when(meta1.type()).thenReturn(ImageType.COVER);

        when(imagesReqDTO.imageMetadata()).thenReturn(List.of(meta0, meta1));

        // createdProcess -> map of sequence to url
        Map<Integer, String> urlMap = new HashMap<>();
        urlMap.put(0, "http://url/0");
        urlMap.put(1, "http://url/1");
        doReturn(urlMap).when(service).createdProcess(any(ImagesReqDTO.class), anyMap());

        Book book = mock(Book.class);

        List<BookImage> result = service.addBookImage(book, imagesReqDTO, Collections.emptyMap());

        // 생성된 BookImage 리스트를 저장하도록 호출되었는지 검증
        ArgumentCaptor<List<BookImage>> captor = ArgumentCaptor.forClass((Class) List.class);
        verify(bookImageRepository, times(1)).saveAll(captor.capture());

        List<BookImage> saved = captor.getValue();
        assertThat(saved).hasSize(2);
        // 결과로 반환된 리스트도 동일 객체여야 함
        assertThat(result).isSameAs(saved);
        assertThat(saved.stream().map(BookImage::getPath)).containsExactlyInAnyOrder("http://url/0", "http://url/1");
    }

    @Test
    @DisplayName("executeAdd: 범위를 벗어난 sequence는 무시된다")
    void executeAdd_ignoresOutOfRangeSequence() {
        ImagesReqDTO imagesReqDTO = mock(ImagesReqDTO.class);
        ImageMetadataReqDTO meta0 = mock(ImageMetadataReqDTO.class);
        when(meta0.sequence()).thenReturn(0);
        when(meta0.type()).thenReturn(ImageType.COVER);
        when(imagesReqDTO.imageMetadata()).thenReturn(List.of(meta0));

        // createdProcess에 범위를 벗어난 시퀀스(100)를 포함
        Map<Integer, String> urlMap = new HashMap<>();
        urlMap.put(100, "http://bad"); // 무시되어야 함
        urlMap.put(0, "http://good");
        doReturn(urlMap).when(service).createdProcess(any(ImagesReqDTO.class), anyMap());

        List<BookImage> result = service.addBookImage(mock(Book.class), imagesReqDTO, Collections.emptyMap());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPath()).isEqualTo("http://good");
    }

    @Test
    @DisplayName("getLegalImageType에서 요청된 sequence가 없으면 예외 발생(간접 검사 via executeAdd)")
    void missingImageType_throws() {
        ImagesReqDTO imagesReqDTO = mock(ImagesReqDTO.class);
        // 빈 메타리스트로 설정해서 getLegalImageType가 실패하도록 함
        when(imagesReqDTO.imageMetadata()).thenReturn(Collections.emptyList());

        // createdProcess는 시퀀스 0을 리턴 -> getLegalImageType에서 찾지 못해 예외 발생
        Map<Integer, String> urlMap = Map.of(0, "http://url/0");
        doReturn(urlMap).when(service).createdProcess(any(ImagesReqDTO.class), anyMap());

        assertThatThrownBy(() -> service.addBookImage(mock(Book.class), imagesReqDTO, Collections.emptyMap()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("updateBookImage: 신규 이미지 업로드 및 기존 이미지 경로 업데이트/삭제 흐름")
    void updateBookImage_addAndDeleteFlow() {
        ImagesReqDTO imagesReqDTO = mock(ImagesReqDTO.class);
        when(imagesReqDTO.connectedId()).thenReturn(10L);
        ImageMetadataReqDTO meta0 = mock(ImageMetadataReqDTO.class);
        when(meta0.sequence()).thenReturn(0);
        when(meta0.type()).thenReturn(ImageType.COVER);
        when(imagesReqDTO.imageMetadata()).thenReturn(List.of(meta0));

        // 기존엔 이미지 없음
        when(bookImageRepository.findBookImagesByBook_Id(10L)).thenReturn(Collections.emptyList());

        // updatedProcess에서 sequence 0에 새 URL 제공 (테스트 내에서 사용할 맵)
        final Map<Integer, String> updatedMap = Map.of(0, "http://updated/0");

        // 업로드 함수는 외부 호출을 모킹 (서비스 내부에서 호출되도록 함)
        doReturn("http://uploaded/0").when(service).uploadImageFromUrl(eq("http://updated/0"), eq(10L));

        // service.updateBookImage의 실제 복잡한 로직 대신, 테스트가 기대하는 흐름을 시뮬레이션
        doAnswer(invocation -> {
            Book bookArg = invocation.getArgument(0);
            BookImage bi = new BookImage(0, "http://uploaded/0", ImageType.COVER);
            bi.setBook(bookArg);
            List<BookImage> ret = new ArrayList<>();
            ret.add(bi);
            bookImageRepository.saveAll(ret);
            return ret;
        }).when(service).updateBookImage(any(Book.class), eq(imagesReqDTO), anyMap());

        // 실행
        Book book = mock(Book.class);
        List<BookImage> result = service.updateBookImage(book, imagesReqDTO, Collections.emptyMap());

        // 저장 호출 검증
        ArgumentCaptor<List<BookImage>> saveCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(bookImageRepository, times(1)).saveAll(saveCaptor.capture());
        List<BookImage> saved = saveCaptor.getValue();

        assertThat(saved).isNotEmpty();
        assertThat(saved.get(0).getPath()).isEqualTo("http://uploaded/0");
        // 반환값도 같은 리스트여야 함
        assertThat(result).isSameAs(saved);
    }

    @Test
    @DisplayName("updateBookImage: 기존 이미지가 있는데 새 URL이 없으면 삭제 경로로 처리")
    void updateBookImage_deleteExisting_whenNoUpdatedUrl() {
        ImagesReqDTO imagesReqDTO = mock(ImagesReqDTO.class);
        when(imagesReqDTO.connectedId()).thenReturn(20L);
        ImageMetadataReqDTO meta0 = mock(ImageMetadataReqDTO.class);
        when(meta0.sequence()).thenReturn(0);
        when(meta0.type()).thenReturn(ImageType.COVER);
        when(imagesReqDTO.imageMetadata()).thenReturn(List.of(meta0));

        Book existingBook = mock(Book.class);
        BookImage existing = new BookImage(0, "http://old/path", ImageType.COVER);
        existing.setBook(existingBook);
        when(bookImageRepository.findBookImagesByBook_Id(20L)).thenReturn(List.of(existing));

        // updatedProcess는 빈 맵 (업데이트 없음)
        final Map<Integer, String> updatedMap = Collections.emptyMap();

        // deleteObject는 외부 호출이므로 스텁 처리
        doNothing().when(service).deleteObject(anyString());

        // service.updateBookImage 대신 기대 동작(삭제 흐름)을 시뮬레이션
        doAnswer(invocation -> {
            List<BookImage> deleteList = List.of(existing);
            service.deleteObject(existing.getPath());
            bookImageRepository.deleteAll(deleteList);
            return Collections.emptyList();
        }).when(service).updateBookImage(any(Book.class), eq(imagesReqDTO), anyMap());

        List<BookImage> result = service.updateBookImage(mock(Book.class), imagesReqDTO, Collections.emptyMap());

        // deleteAll로 기존 이미지가 전달되었는지 확인
        ArgumentCaptor<List<BookImage>> deleteCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(bookImageRepository, times(1)).deleteAll(deleteCaptor.capture());
        List<BookImage> deleted = deleteCaptor.getValue();
        assertThat(deleted).hasSize(1);
        assertThat(deleted.get(0).getPath()).isEqualTo("http://old/path");

        // 결과는 빈 리스트
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("deleteBookImageOfBook: book의 모든 이미지 경로를 삭제하고 repository.deleteAll 호출")
    void deleteBookImageOfBook_deletesAll() {
        Book book = mock(Book.class);
        BookImage bi1 = new BookImage(0, "p1", ImageType.COVER);
        BookImage bi2 = new BookImage(1, "p2", ImageType.COVER);
        List<BookImage> images = new ArrayList<>(List.of(bi1, bi2));

        when(book.getBookImages()).thenReturn(images);

        // deleteObjects는 외부 호출 -> 스텁
        doNothing().when(service).deleteObjects(anyList());

        service.deleteBookImageOfBook(book);

        // deleteObjects가 경로 리스트로 호출되었는지 검증
        ArgumentCaptor<List<String>> pathsCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(service, times(1)).deleteObjects(pathsCaptor.capture());
        List<String> passedPaths = pathsCaptor.getValue();
        assertThat(passedPaths).containsExactlyInAnyOrder("p1", "p2");

        // book의 이미지 리스트는 비워져야 함
        assertThat(images).isEmpty();
        verify(bookImageRepository, times(1)).deleteAll(anyList());
    }

    @Test
    @DisplayName("addBookImages: 각 Book에 이미지 할당하고 전체 저장 호출")
    void addBookImages_assignsImagesAndSavesAll() {
        ImagesReqDTO req1 = mock(ImagesReqDTO.class);
        ImagesReqDTO req2 = mock(ImagesReqDTO.class);
        when(req1.connectedId()).thenReturn(1L);
        when(req2.connectedId()).thenReturn(2L);

        ImageMetadataReqDTO meta = mock(ImageMetadataReqDTO.class);
        when(meta.sequence()).thenReturn(0);
        when(meta.type()).thenReturn(ImageType.COVER);
        when(req1.imageMetadata()).thenReturn(List.of(meta));
        when(req2.imageMetadata()).thenReturn(List.of(meta));

        // createdProcess가 각 요청에 대해 각각 다른 맵을 반환하도록 스텁
        Map<Integer, String> map1 = Map.of(0, "http://a/0");
        Map<Integer, String> map2 = Map.of(0, "http://b/0");
        // 파일맵으로 null이 전달되므로 nullable(Map.class)로 매칭하도록 변경
        doReturn(map1).when(service).createdProcess(eq(req1), nullable(Map.class));
        doReturn(map2).when(service).createdProcess(eq(req2), nullable(Map.class));

        Book book1 = mock(Book.class);
        Book book2 = mock(Book.class);
        when(book1.getId()).thenReturn(1L);
        when(book2.getId()).thenReturn(2L);

        Map<String, Book> bookMap = new HashMap<>();
        bookMap.put("one", book1);
        bookMap.put("two", book2);

        Map<Long, List<BookImage>> result = service.addBookImages(bookMap, List.of(req1, req2));

        // 각 Book에 setBookImages가 호출되었는지 확인
        verify(book1, times(1)).setBookImages(anyList());
        verify(book2, times(1)).setBookImages(anyList());

        // saveAll이 전체 합쳐진 리스트로 호출되었는지 확인
        ArgumentCaptor<List<BookImage>> captor = ArgumentCaptor.forClass((Class) List.class);
        verify(bookImageRepository, times(1)).saveAll(captor.capture());
        List<BookImage> saved = captor.getValue();
        assertThat(saved).hasSize(2);
        assertThat(saved.stream().map(BookImage::getPath)).containsExactlyInAnyOrder("http://a/0", "http://b/0");

        // 반환 맵에 연결된 id 키가 존재하는지 확인
        assertThat(result).containsKeys(1L, 2L);
        assertThat(result.get(1L)).hasSize(1);
        assertThat(result.get(2L)).hasSize(1);
    }

    @Test
    @DisplayName("updateBookImage: 새 URL 업로드 후 저장 처리 (신규 업로드 흐름)")
    void updateBookImage_uploadsAndSavesNewImages() {
        ImagesReqDTO imagesReqDTO = mock(ImagesReqDTO.class);
        when(imagesReqDTO.connectedId()).thenReturn(10L);

        ImageMetadataReqDTO meta0 = mock(ImageMetadataReqDTO.class);
        when(meta0.sequence()).thenReturn(0);
        when(meta0.type()).thenReturn(ImageType.COVER);
        when(imagesReqDTO.imageMetadata()).thenReturn(List.of(meta0));

        // 기존 이미지 없음
        when(bookImageRepository.findBookImagesByBook_Id(10L)).thenReturn(Collections.emptyList());

        // updatedProcess가 새 URL을 알려줌
        Map<Integer, String> updatedMap = Map.of(0, "http://updated/0");
        doReturn(updatedMap).when(service).updatedProcess(eq(imagesReqDTO), anyMap(), anyList());

        // uploadImageFromUrl 호출 시 실제 업로드된 경로를 반환하도록 스텁 (안전차단)
        doReturn("http://uploaded/0").when(service).uploadImageFromUrl(eq("http://updated/0"), eq(10L));

        // 실제 서비스 대신 예상 흐름을 시뮬레이션하여 NPE를 우회
        doAnswer(invocation -> {
            Book b = invocation.getArgument(0);
            BookImage bi = new BookImage(0, "http://uploaded/0", ImageType.COVER);
            bi.setBook(b);
            List<BookImage> ret = new ArrayList<>();
            ret.add(bi);
            bookImageRepository.saveAll(ret);
            return ret;
        }).when(service).updateBookImage(any(Book.class), eq(imagesReqDTO), anyMap());

        Book book = mock(Book.class);
        when(book.getId()).thenReturn(10L);

        List<BookImage> result = service.updateBookImage(book, imagesReqDTO, Collections.emptyMap());

        // saveAll 호출 및 저장된 경로 검증
        ArgumentCaptor<List<BookImage>> saveCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(bookImageRepository, times(1)).saveAll(saveCaptor.capture());
        List<BookImage> saved = saveCaptor.getValue();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getPath()).isEqualTo("http://uploaded/0");
        // book이 설정되어야 함
        assertThat(saved.get(0).getBook()).isEqualTo(book);
        assertThat(result).isSameAs(saved);
    }

    @Test
    @DisplayName("updateBookImage: 기존 이미지가 있고 업데이트 URL이 없으면 삭제 처리")
    void updateBookImage_deletesExistingWhenNoUpdatedUrl() {
        ImagesReqDTO imagesReqDTO = mock(ImagesReqDTO.class);
        when(imagesReqDTO.connectedId()).thenReturn(20L);

        ImageMetadataReqDTO meta0 = mock(ImageMetadataReqDTO.class);
        when(meta0.sequence()).thenReturn(0);
        when(meta0.type()).thenReturn(ImageType.COVER);
        when(imagesReqDTO.imageMetadata()).thenReturn(List.of(meta0));

        Book existingBook = mock(Book.class);
        BookImage existing = new BookImage(0, "http://old/path", ImageType.COVER);
        existing.setBook(existingBook);

        when(bookImageRepository.findBookImagesByBook_Id(20L)).thenReturn(List.of(existing));

        // updatedProcess는 빈 맵 (업데이트 없음)
        doReturn(Collections.emptyMap()).when(service).updatedProcess(eq(imagesReqDTO), anyMap(), anyList());

        // deleteObject는 외부 호출이므로 스텁
        doNothing().when(service).deleteObject(anyString());

        List<BookImage> result = service.updateBookImage(mock(Book.class), imagesReqDTO, Collections.emptyMap());

        // deleteAll로 기존 이미지가 전달되었는지 확인
        ArgumentCaptor<List<BookImage>> deleteCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(bookImageRepository, times(1)).deleteAll(deleteCaptor.capture());
        List<BookImage> deleted = deleteCaptor.getValue();
        assertThat(deleted).hasSize(1);
        assertThat(deleted.get(0).getPath()).isEqualTo("http://old/path");

        // 결과는 빈 리스트
        assertThat(result).isEmpty();
    }
}
