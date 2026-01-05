package com.daisobook.shop.booksearch.BooksSearch.service;

import com.daisobook.shop.booksearch.BooksSearch.entity.book.BookImage;
import com.daisobook.shop.booksearch.BooksSearch.repository.book.BookImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ImageProcessor {
    private final BookImageRepository bookImageRepository;
    private final MinIOService minIOService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSingleImage(Long imageId) {
        BookImage image = bookImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("이미지를 찾을 수 없음"));

        String currentPath = image.getPath();
        if (currentPath == null || currentPath.contains("your-minio-domain.com")) return;

        Long bookId = (image.getBook() != null) ? image.getBook().getId() : 0L;
        String newMinioUrl = minIOService.uploadImageFromUrl(currentPath, bookId);

        image.setPath(newMinioUrl);
        bookImageRepository.saveAndFlush(image);
    }
}