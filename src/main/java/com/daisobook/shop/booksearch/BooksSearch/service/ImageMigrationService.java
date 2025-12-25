package com.daisobook.shop.booksearch.BooksSearch.service;

import com.daisobook.shop.booksearch.BooksSearch.entity.book.BookImage;
import com.daisobook.shop.booksearch.BooksSearch.repository.book.BookImageRepository;
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
public  class ImageMigrationService {

    private final BookImageRepository bookImageRepository;
    private final MinIOService minIOService;

    /**
     * 외부 URL로 저장된 모든 BookImage를 MinIO로 이전합니다.
     */
//    @Transactional
//    public void migrateAllImages() {
//        // 1. 전체 이미지 데이터 조회
//        List<BookImage> allImages = bookImageRepository.findAll();
//        log.info("총 {}건의 이미지 마이그레이션을 시작합니다.", allImages.size());
//
//        int successCount = 0;
//        int skipCount = 0;
//        int failCount = 0;
//
//        for (BookImage bookImage : allImages) {
//            String currentPath = bookImage.getPath();
//
//            // 2. 이미 MinIO URL이거나 비어있으면 스킵 (필요에 따라 조건 수정)
//            if (currentPath == null || !currentPath.startsWith("http")) {
//                skipCount++;
//                continue;
//            }
//
//            // 본인 서버의 MinIO URL이 이미 포함되어 있다면 중복 업로드 방지
//            if (currentPath.contains("your-minio-domain.com")) {
//                skipCount++;
//                continue;
//            }
//
//            try {
//                // 3. MinIOService를 사용하여 업로드 (book_id와 기존 path 전달)
//                // book_id가 null일 경우를 대비해 기본값을 넣거나 예외처리가 필요할 수 있습니다.
//                Long bookId = (bookImage.getBook() != null) ? bookImage.getBook().getId() : 0L;
//
//                String newMinioUrl = minIOService.uploadImageFromUrl(currentPath, bookId);
//
//                // 4. DB 엔티티 업데이트 (Setter 사용)
//                bookImage.setPath(newMinioUrl);
//
//                successCount++;
//                log.info("[성공] ID: {} -> 새 경로: {}", bookImage.getId(), newMinioUrl);
//
//            } catch (Exception e) {
//                failCount++;
//                log.error("[실패] ID: {} - 사유: {}", bookImage.getId(), e.getMessage());
//            }
//        }
//
//        log.info("마이그레이션 완료! (성공: {}, 스킵: {}, 실패: {})", successCount, skipCount, failCount);
//    }

    // 핵심: 여기에는 @Transactional을 절대 붙이지 마세요!
    public void migrateInBatches() {
        int pageSize = 100;

        while (true) {
            // 1. 커넥션을 잠깐 쓰고 바로 반환하도록 페이징 조회
            List<BookImage> targetImages = getTargetImages(pageSize);

            if (targetImages.isEmpty()) {
                log.info("마이그레이션 완료.");
                break;
            }

            for (BookImage image : targetImages) {
                try {
                    // 2. 개별 처리에만 트랜잭션 적용
                    processSingleImage(image.getId());
                } catch (Exception e) {
                    log.error("ID {} 처리 실패: {}", image.getId(), e.getMessage());
                }
            }
        }
    }

    // 별도의 읽기 전용 트랜잭션 (커넥션을 금방 반납함)
    @Transactional(readOnly = true)
    public List<BookImage> getTargetImages(int size) {
        return bookImageRepository.findImagesToMigrate(PageRequest.of(0, size));
    }

    // 개별 저장용 트랜잭션 (매 호출마다 커넥션 1개를 짧게 사용 후 반납)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSingleImage(Long imageId) {
        // ID로 새로 조회하여 영속성 컨텍스트를 분리
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