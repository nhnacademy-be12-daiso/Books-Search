package com.daisobook.shop.booksearch.BooksSearch.service;

import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.BookImage;
import com.daisobook.shop.booksearch.BooksSearch.repository.book.BookImageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageMigrationServiceTest {

    @MockitoBean
    @Mock
    private BookImageRepository bookImageRepository;

    @MockitoBean @Mock
    private MinIOService minIOService;

    @InjectMocks
    private ImageMigrationService imageMigrationService;

    @Test
    @DisplayName("TargetImages 조회 - Repository에 위임하고 결과 반환")
    void getTargetImages_delegatesToRepository_and_returnsList() {
        List<BookImage> expected = List.of(mock(BookImage.class));
        when(bookImageRepository.findImagesToMigrate(any())).thenReturn(expected);

        List<BookImage> actual = imageMigrationService.getTargetImages(50);

        assertSame(expected, actual);
        verify(bookImageRepository, times(1)).findImagesToMigrate(any());
    }

    @Test
    @DisplayName("단일 이미지 처리 - 성공 시 경로 업데이트 및 저장")
    void processSingleImage_success_updatesPath_and_saves() {
        Long id = 1L;
        BookImage img = mock(BookImage.class);
        Book book = mock(Book.class);

        when(bookImageRepository.findById(id)).thenReturn(Optional.of(img));
        when(img.getPath()).thenReturn("http://external.example/image.jpg");
        when(img.getBook()).thenReturn(book);
        when(book.getId()).thenReturn(42L);

        when(minIOService.uploadImageFromUrl("http://external.example/image.jpg", 42L)).thenReturn("http://minio/new.jpg");

        imageMigrationService.processSingleImage(id);

        // verify upload called with correct args and entity updated & saved
        verify(minIOService, times(1)).uploadImageFromUrl("http://external.example/image.jpg", 42L);
        verify(img, times(1)).setPath("http://minio/new.jpg");
        verify(bookImageRepository, times(1)).saveAndFlush(img);
    }

    @Test
    @DisplayName("단일 이미지 처리 - 경로가 null이거나 도메인 포함 시 스킵")
    void processSingleImage_skips_whenPathNull_orContainsDomain() {
        Long id1 = 10L;
        BookImage img1 = mock(BookImage.class);
        when(bookImageRepository.findById(id1)).thenReturn(Optional.of(img1));
        when(img1.getPath()).thenReturn(null);

        imageMigrationService.processSingleImage(id1);
        verify(minIOService, never()).uploadImageFromUrl(anyString(), anyLong());
        verify(bookImageRepository, never()).saveAndFlush(any());

        Long id2 = 11L;
        BookImage img2 = mock(BookImage.class);
        when(bookImageRepository.findById(id2)).thenReturn(Optional.of(img2));
        when(img2.getPath()).thenReturn("https://your-minio-domain.com/some.jpg");

        imageMigrationService.processSingleImage(id2);
        verify(minIOService, never()).uploadImageFromUrl(anyString(), anyLong());
        verify(bookImageRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("단일 이미지 처리 - 이미지가 없으면 RuntimeException 발생")
    void processSingleImage_notFound_throwsRuntime() {
        Long missing = 999L;
        when(bookImageRepository.findById(missing)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> imageMigrationService.processSingleImage(missing));
    }

    @Test
    @DisplayName("배치 마이그레이션 - 모든 페이지 처리 후 종료")
    void migrateInBatches_processesAllPages_and_stopsWhenEmpty() {
        // Prepare two BookImage projections with ids 1L and 2L returned on first page,
        // then empty on next call to stop loop.
        BookImage bi1 = mock(BookImage.class);
        BookImage bi2 = mock(BookImage.class);
        when(bi1.getId()).thenReturn(1L);
        when(bi2.getId()).thenReturn(2L);

        when(bookImageRepository.findImagesToMigrate(any()))
                .thenReturn(List.of(bi1, bi2))
                .thenReturn(Collections.emptyList());

        // findById for each id used by processSingleImage
        BookImage full1 = mock(BookImage.class);
        BookImage full2 = mock(BookImage.class);
        Book book1 = mock(Book.class);
        Book book2 = mock(Book.class);

        when(bookImageRepository.findById(1L)).thenReturn(Optional.of(full1));
        when(full1.getPath()).thenReturn("http://a/1.jpg");
        when(full1.getBook()).thenReturn(book1);
        when(book1.getId()).thenReturn(101L);
        when(minIOService.uploadImageFromUrl("http://a/1.jpg", 101L)).thenReturn("minio-1");
        // ensure save works
        doNothing().when(full1).setPath(anyString());

        when(bookImageRepository.findById(2L)).thenReturn(Optional.of(full2));
        when(full2.getPath()).thenReturn("http://a/2.jpg");
        when(full2.getBook()).thenReturn(book2);
        when(book2.getId()).thenReturn(202L);
        when(minIOService.uploadImageFromUrl("http://a/2.jpg", 202L)).thenReturn("minio-2");
        doNothing().when(full2).setPath(anyString());

        // run migration loop
        imageMigrationService.migrateInBatches();

        // verify repository paging called at least twice (first non-empty, then empty)
        verify(bookImageRepository, atLeast(2)).findImagesToMigrate(any());
        // verify per-image processing interactions
        verify(minIOService, times(1)).uploadImageFromUrl("http://a/1.jpg", 101L);
        verify(minIOService, times(1)).uploadImageFromUrl("http://a/2.jpg", 202L);
        verify(bookImageRepository, times(1)).saveAndFlush(full1);
        verify(bookImageRepository, times(1)).saveAndFlush(full2);
    }
}
