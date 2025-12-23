package com.daisobook.shop.booksearch.BooksSearch.search.component.assembler;

import com.daisobook.shop.booksearch.BooksSearch.search.domain.Book;
import com.daisobook.shop.booksearch.BooksSearch.search.dto.BookResponseDto;
import com.daisobook.shop.booksearch.BooksSearch.search.dto.SearchResponseDto;
import com.daisobook.shop.booksearch.BooksSearch.search.component.BookMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SearchResultAssembler {

    private final BookMapper bookMapper;
    private static final int FINAL_RESULT_SIZE = 30;

    // 일반 검색 결과 조립(ES에 저장된 aiResult는 BookMapper가 그대로 붙여줌)
    public SearchResponseDto assembleBasicResult(List<Book> books) {
        List<BookResponseDto> dtos = books.stream()
                .limit(FINAL_RESULT_SIZE)
                .map(b -> bookMapper.toDto(b, 50))
                .toList();
        return SearchResponseDto.builder().bookList(dtos).build();
    }
}
