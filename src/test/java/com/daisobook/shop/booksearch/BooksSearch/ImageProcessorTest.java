package com.daisobook.shop.booksearch.BooksSearch;

import com.daisobook.shop.booksearch.BooksSearch.entity.book.Book;
import com.daisobook.shop.booksearch.BooksSearch.entity.book.BookImage;
import com.daisobook.shop.booksearch.BooksSearch.repository.book.BookImageRepository;
import com.daisobook.shop.booksearch.BooksSearch.service.ImageProcessor;
import com.daisobook.shop.booksearch.BooksSearch.service.MinIOService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageProcessorTest {

    @Mock
    private BookImageRepository bookImageRepository;

    @Mock
    private MinIOService minIOService;

    @InjectMocks
    private ImageProcessor imageProcessor;

    @Test
    @DisplayName("processSingleImage - 성공 시 업로드 및 DB 업데이트")
    void processSingleImage_success() {
        Long id = 1L;
        BookImage img = mock(BookImage.class);
        Book book = mock(Book.class);

        when(bookImageRepository.findById(id)).thenReturn(Optional.of(img));
        when(img.getPath()).thenReturn("http://old-path.jpg");
        when(img.getBook()).thenReturn(book);
        when(book.getId()).thenReturn(100L);
        when(minIOService.uploadImageFromUrl("http://old-path.jpg", 100L)).thenReturn("http://minio-new-path.jpg");

        imageProcessor.processSingleImage(id);

        verify(minIOService).uploadImageFromUrl(anyString(), anyLong());
        verify(img).setPath("http://minio-new-path.jpg");
        verify(bookImageRepository).saveAndFlush(img);
    }

    @Test
    @DisplayName("이미지가 없으면 RuntimeException 발생")
    void processSingleImage_notFound_throws() {
        when(bookImageRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> imageProcessor.processSingleImage(999L));
    }
}