package com.daisobook.shop.booksearch.repository.book;

import com.daisobook.shop.booksearch.entity.ImageType;
import com.daisobook.shop.booksearch.entity.book.Book;
import com.daisobook.shop.booksearch.entity.book.BookImage;
import com.daisobook.shop.booksearch.entity.book.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookImageRepositoryTest {

    @Autowired
    private BookImageRepository bookImageRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Book book;

    @BeforeEach
    void setUp() {
        // Book 생성 (이미 정의된 생성자 활용)
        book = new Book("9788998139766", "테스트 도서", "인덱스", "설명", 
                        LocalDate.now(), 20000L, true, 100, Status.ON_SALE, 1);
        entityManager.persist(book);
    }

    @Test
    @DisplayName("도서 ID로 이미지 목록을 조회한다")
     void findBookImagesByBook_IdTest() {
        BookImage path1 = new BookImage(1, "path1", ImageType.COVER);
        BookImage path2 = new BookImage(2, "path2", ImageType.DETAIL);

        path1.setBook(book);
        path2.setBook(book);

        bookImageRepository.save(path1);
        bookImageRepository.save(path2);

        List<BookImage> images = bookImageRepository.findBookImagesByBook_Id(book.getId());

        assertThat(images).hasSize(2);
    }

    @Test
    @DisplayName("마이그레이션 대상 이미지만 조회한다 (외부 URL이면서 특정 도메인 제외)")
    void findImagesToMigrateTest() {
        // 1. 대상: 외부 http 경로
        bookImageRepository.save(new BookImage(1, "http://external.com/img.jpg", ImageType.COVER));
        // 2. 비대상: 이미 마이그레이션된 경로
        bookImageRepository.save(new BookImage(2, "http://storage.java21.net/img.jpg", ImageType.DETAIL));
        // 3. 비대상: 로컬 경로
        bookImageRepository.save(new BookImage(3, "/local/path.jpg", ImageType.DETAIL));

        List<BookImage> result = bookImageRepository.findImagesToMigrate(PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getPath()).isEqualTo("http://external.com/img.jpg");
    }
}