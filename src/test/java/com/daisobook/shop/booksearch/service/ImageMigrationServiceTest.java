package com.daisobook.shop.booksearch.service;

import com.daisobook.shop.booksearch.entity.book.BookImage;
import com.daisobook.shop.booksearch.repository.book.BookImageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageMigrationServiceTest {

    @Mock
    private BookImageRepository bookImageRepository;

    @Mock
    private ImageProcessor imageProcessor; // 새로 분리한 서비스를 Mock으로 선언

    @InjectMocks
    private ImageMigrationService imageMigrationService;

    @Test
    @DisplayName("getTargetImages - Repository 호출 확인")
    void getTargetImages_delegatesToRepository() {
        List<BookImage> expected = List.of(mock(BookImage.class));
        when(bookImageRepository.findImagesToMigrate(any())).thenReturn(expected);

        List<BookImage> actual = imageMigrationService.getTargetImages(50);

        assertSame(expected, actual);
        verify(bookImageRepository).findImagesToMigrate(any());
    }

    @Test
    @DisplayName("migrateInBatches - 루프를 돌며 ImageProcessor를 호출하는지 확인")
    void migrateInBatches_callsProcessorForEachImage() {
        // [Given] 첫 번째 호출에 2개, 두 번째 호출에 빈 리스트 반환 (루프 종료용)
        BookImage bi1 = mock(BookImage.class);
        BookImage bi2 = mock(BookImage.class);
        when(bi1.getId()).thenReturn(1L);
        when(bi2.getId()).thenReturn(2L);

        when(bookImageRepository.findImagesToMigrate(any()))
                .thenReturn(List.of(bi1, bi2))
                .thenReturn(Collections.emptyList());

        // [When] 실행
        imageMigrationService.migrateInBatches();

        // [Then] ImageProcessor의 processSingleImage가 각 ID에 대해 호출되었는지 검증
        verify(imageProcessor, times(1)).processSingleImage(1L);
        verify(imageProcessor, times(1)).processSingleImage(2L);

        // 배치를 위해 최소 2번의 조회가 일어났는지 확인
        verify(bookImageRepository, atLeast(2)).findImagesToMigrate(any());
    }
}
