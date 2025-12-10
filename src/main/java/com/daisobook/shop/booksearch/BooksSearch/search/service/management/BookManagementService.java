package com.daisobook.shop.booksearch.BooksSearch.search.service.management;

import com.daisobook.shop.booksearch.BooksSearch.search.domain.Book;
import com.daisobook.shop.booksearch.BooksSearch.search.dto.BookJsonDto;
import com.daisobook.shop.booksearch.BooksSearch.search.repository.BookRepository;
import com.daisobook.shop.booksearch.BooksSearch.search.service.IndexSetupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookManagementService {

    private final BookRepository bookRepository;
    private final IndexSetupService indexSetupService;

    // 분리된 컴포넌트들
    private final BulkImportExecutor bulkImportExecutor;
    private final BookDataProcessor dataProcessor;

    // 1. 대량 등록
    public String importBooks(String fileName) {
        indexSetupService.createIndex(); // 인덱스 보장

        int count = bulkImportExecutor.execute(fileName);

        return (count == 0)
                ? "모든 데이터가 이미 최신 상태입니다."
                : String.format("총 %d권 대량 등록 완료.", count);
    }

    // 2. 단건 등록 및 수정
    public String upsertBook(BookJsonDto dto) {
        Optional<Book> existingOpt = bookRepository.findByIsbn(dto.getIsbn()).stream().findFirst();

        if (existingOpt.isPresent()) {
            // Processor에게 병합 위임
            Book existingBook = existingOpt.get();
            dataProcessor.mergeBook(existingBook, dto);

            bookRepository.save(existingBook);
            return "도서 정보가 수정되었습니다: " + existingBook.getTitle();
        } else {
            // Processor에게 생성 위임
            Book newBook = dataProcessor.createBook(dto);

            bookRepository.save(newBook);
            return "새로운 도서가 등록되었습니다: " + newBook.getTitle();
        }
    }
}