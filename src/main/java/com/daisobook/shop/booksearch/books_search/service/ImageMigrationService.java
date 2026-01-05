package com.daisobook.shop.booksearch.books_search.service;

import com.daisobook.shop.booksearch.books_search.entity.book.BookImage;
import com.daisobook.shop.booksearch.books_search.repository.book.BookImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageMigrationService {
    private final ImageProcessor imageProcessor; // 분리된 서비스 주입
    private final BookImageRepository bookImageRepository;

    @Transactional(propagation = Propagation.REQUIRED)
    public void migrateInBatches() {
        int pageSize = 100;
        while (true) {
            List<BookImage> targetImages = getTargetImages(pageSize);
            if (targetImages.isEmpty()) break;

            for (BookImage image : targetImages) {
                try {
                    // 이제 외부 빈을 호출하므로 @Transactional이 정상 작동함!
                    imageProcessor.processSingleImage(image.getId());
                } catch (Exception e) {
                    log.error("ID {} 처리 실패", image.getId(), e);
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public List<BookImage> getTargetImages(int size) {
        return bookImageRepository.findImagesToMigrate(PageRequest.of(0, size));
    }
}