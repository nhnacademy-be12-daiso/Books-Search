package com.daisobook.shop.booksearch.BooksSearch.search.service;

import com.daisobook.shop.booksearch.BooksSearch.search.domain.Book;
import com.daisobook.shop.booksearch.BooksSearch.search.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookManagementService {

    private final BookRepository bookRepository;

    /**
     * 도서 등록 및 수정 (Atomic 보장)
     */
    @Transactional // 쓰기 트랜잭션 필수
    public void upsertBook(Book book) {
        try {
            // Elasticsearch는 id가 같으면 덮어쓰기(Update)가 됨
            bookRepository.save(book);
            log.info("✅ 도서 저장 성공: ID={}, Title={}", book.getId(), book.getTitle());
        } catch (Exception e) {
            log.error("❌ 도서 저장 실패: ID={}", book.getId(), e);
            throw new RuntimeException("도서 저장 중 데이터베이스 오류 발생", e);
        }
    }

    @Transactional
    public void deleteBook(String id) {
        try {
            bookRepository.deleteById(id);
            log.info("🗑️ 도서 삭제 성공: ID={}", id);
        } catch (Exception e) {
            log.error("❌ 도서 삭제 실패: ID={}", id, e);
            throw new RuntimeException("도서 삭제 실패", e);
        }
    }
}