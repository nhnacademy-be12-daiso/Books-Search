package com.daisobook.shop.booksearch.BooksSearch.search.service.management;

import com.daisobook.shop.booksearch.BooksSearch.search.domain.Book;
import com.daisobook.shop.booksearch.BooksSearch.search.dto.BookJsonDto;
import com.daisobook.shop.booksearch.BooksSearch.search.repository.BookRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class BulkImportExecutor {

    private final BookRepository bookRepository;
    private final BookDataProcessor dataProcessor;
    private final ObjectMapper objectMapper;

    // 인프라 설정 (스레드풀, 세마포어)
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final Semaphore semaphore = new Semaphore(2);

    public int execute(String fileName) {
        try {
            // 1. 파일 로드 및 필터링
            List<BookJsonDto> targets = loadAndFilter(fileName);
            if (targets.isEmpty()) return 0;

            log.info("대량 임포트 시작: {}권", targets.size());

            // 2. 병렬 처리 준비
            AtomicInteger counter = new AtomicInteger(0);
            List<Book> buffer = new ArrayList<>();
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            // 3. 비동기 작업 제출
            for (BookJsonDto dto : targets) {
                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        semaphore.acquire(); // 부하 제어
                        processSingleItem(dto, buffer, counter);
                    } catch (Exception e) {
                        log.error("처리 실패 [ISBN: {}]", dto.getIsbn());
                    } finally {
                        semaphore.release();
                    }
                }, executor));
            }

            // 4. 완료 대기 및 잔여 저장
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            flushBuffer(buffer, counter);

            return counter.get();

        } catch (Exception e) {
            throw new RuntimeException("임포트 실행 중 오류", e);
        }
    }

    private void processSingleItem(BookJsonDto dto, List<Book> buffer, AtomicInteger counter) {
        if (dataProcessor.isCategoryMissing(dto)) return;

        // Processor에게 변환 위임
        Book book = dataProcessor.createBook(dto);

        synchronized (buffer) {
            buffer.add(book);
            if (buffer.size() >= 100) {
                flushBuffer(buffer, counter);
            }
        }
    }

    private void flushBuffer(List<Book> buffer, AtomicInteger counter) {
        synchronized (buffer) {
            if (!buffer.isEmpty()) {
                bookRepository.saveAll(new ArrayList<>(buffer));
                counter.addAndGet(buffer.size());
                buffer.clear();
            }
        }
    }

    private List<BookJsonDto> loadAndFilter(String fileName) throws Exception {
        ClassPathResource resource = new ClassPathResource(fileName);
        List<BookJsonDto> allBooks;
        try (InputStream is = resource.getInputStream()) {
            allBooks = objectMapper.readValue(is, new TypeReference<>() {});
        }

        Set<String> savedIsbns = bookRepository.findAllIsbns();
        return allBooks.stream()
                .filter(d -> d.getIsbn() != null && !savedIsbns.contains(d.getIsbn()))
                .distinct()
                .toList();
    }
}