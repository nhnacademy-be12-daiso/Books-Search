package com.daisobook.shop.booksearch.BooksSearch.search.service.search;


import com.daisobook.shop.booksearch.BooksSearch.search.config.SearchUtils;
import com.daisobook.shop.booksearch.BooksSearch.search.domain.Book;
import com.daisobook.shop.booksearch.BooksSearch.search.dto.AiResultDto;
import com.daisobook.shop.booksearch.BooksSearch.search.dto.BookResponseDto;
import com.daisobook.shop.booksearch.BooksSearch.search.dto.BookWithScore;
import com.daisobook.shop.booksearch.BooksSearch.search.dto.SearchResponseDto;
import com.daisobook.shop.booksearch.BooksSearch.search.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final BookRepository bookRepository;
    private final HybridSearchEngine searchEngine; // 검색 담당
    private final AiProviderService aiProvider;    // 평가 담당
    private final BookMapper bookMapper;           // 변환 담당

    // 1. 일반 검색
    public SearchResponseDto basicSearch(String userQuery) {
        // A. ISBN 검색
        if (userQuery.matches("^[0-9-]+$")) {
            List<Book> isbnBooks = bookRepository.findByIsbn(userQuery);
            if (!isbnBooks.isEmpty()) {
                // 바로 변환해서 리턴 (점수 100점)
                return SearchResponseDto.builder()
                        .bookList(bookMapper.toDtoList(isbnBooks, 100))
                        .build();
            }
        }

        // B. 하이브리드 검색
        String refinedQuery = SearchUtils.extractKeywords(userQuery);
        List<BookWithScore> scoredBooks = searchEngine.searchAndRerank(refinedQuery, 50);

        // C. DTO 변환 (Rerank 점수 -> 시그모이드 변환)
        List<BookResponseDto> dtos = scoredBooks.stream()
                .map(bs -> bookMapper.toDto(bs.book(), SearchUtils.calculateSigmoidScore(bs.score())))
                .toList();

        return SearchResponseDto.builder().bookList(dtos).build();
    }

    // 2. AI 검색
    public SearchResponseDto aiSearch(String userQuery) {
        String refinedQuery = SearchUtils.extractKeywords(userQuery);

        // A. 검색 엔진 가동 (Engine 위임)
        List<BookWithScore> scoredBooks = searchEngine.searchAndRerank(refinedQuery, 50);
        if (scoredBooks.isEmpty()) return SearchResponseDto.empty();

        // B. 상위 5개 추출
        List<Book> topBooks = scoredBooks.stream()
                .limit(5)
                .map(BookWithScore::book)
                .toList();

        // C. Gemini 평가 요청 (AiProvider 위임)
        Map<String, AiResultDto> aiResults = aiProvider.evaluateBooks(userQuery, topBooks);

        // D. DTO 변환 및 결과 병합 (Mapper 위임)
        List<BookResponseDto> dtos = bookMapper.toDtoList(topBooks, 0); // 일단 0점
        bookMapper.applyAiEvaluation(dtos, aiResults); // AI 점수와 답변 주입

        // E. 최종 정렬 (Mapper가 주입한 matchRate 기준)
        dtos.sort(Comparator.comparingInt(BookResponseDto::getMatchRate).reversed());

        return SearchResponseDto.builder().bookList(dtos).build();
    }
}