package com.daisobook.shop.booksearch.BooksSearch.search.service.management;

import com.daisobook.shop.booksearch.BooksSearch.search.config.SearchUtils;
import com.daisobook.shop.booksearch.BooksSearch.search.domain.Book;
import com.daisobook.shop.booksearch.BooksSearch.search.dto.BookJsonDto;
import com.daisobook.shop.booksearch.BooksSearch.search.service.search.AiProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BookDataProcessor {

    private final AiProviderService aiProvider;

    public Book createBook(BookJsonDto dto) {
        if (isCategoryMissing(dto)) {
            throw new IllegalArgumentException("신규 등록 시 카테고리는 필수입니다.");
        }

        String cleanAuthor = SearchUtils.cleanAuthorName(dto.getAuthor());
        String cleanDesc = SearchUtils.stripHtml(dto.getDescription());
        List<String> categories = extractCategories(dto);

        // 신규 생성은 무조건 임베딩
        List<Double> embedding = generateEmbedding(dto.getTitle(), cleanAuthor, categories, cleanDesc);

        return Book.builder()
                .isbn(dto.getIsbn())
                .title(dto.getTitle())
                .author(cleanAuthor)
                .publisher(dto.getPublisher())
                .description(cleanDesc)
                .imageUrl(dto.getImageUrl())
                .price(dto.getPrice() != null ? dto.getPrice() : 0)
                .pubDate(SearchUtils.parseDate(dto.getPublicationDate()))
                .categories(categories)
                .embedding(embedding)
                .build();
    }

    /**
     * 기존 도서 업데이트 (Partial Update)
     */
    public void mergeBook(Book book, BookJsonDto dto) {
        boolean reEmbeddingNeeded = false;

        if (dto.getTitle() != null) { book.setTitle(dto.getTitle()); reEmbeddingNeeded = true; }
        if (dto.getAuthor() != null) { book.setAuthor(SearchUtils.cleanAuthorName(dto.getAuthor())); reEmbeddingNeeded = true; }
        if (dto.getDescription() != null) { book.setDescription(SearchUtils.stripHtml(dto.getDescription())); reEmbeddingNeeded = true; }

        List<String> newCats = extractCategories(dto);
        if (!newCats.isEmpty()) { book.setCategories(newCats); reEmbeddingNeeded = true; }

        if (dto.getPublisher() != null) book.setPublisher(dto.getPublisher());
        if (dto.getPrice() != null) book.setPrice(dto.getPrice());
        if (dto.getImageUrl() != null) book.setImageUrl(dto.getImageUrl());
        if (dto.getPublicationDate() != null) book.setPubDate(SearchUtils.parseDate(dto.getPublicationDate()));

        // 텍스트 변경 시 임베딩 재생성
        if (reEmbeddingNeeded) {
            book.setEmbedding(generateEmbedding(book.getTitle(), book.getAuthor(), book.getCategories(), book.getDescription()));
        }
    }

    public boolean isCategoryMissing(BookJsonDto dto) {
        return (dto.getCategory1() == null && dto.getCategory2() == null && dto.getCategory3() == null);
    }

    private List<Double> generateEmbedding(String title, String author, List<String> categories, String desc) {
        String text = String.format("제목: %s, 저자: %s, 카테고리: %s, 설명: %s",
                title, author, String.join(", ", categories), desc);
        if (text.length() > 1000) text = text.substring(0, 1000);

        List<Float> floatVec = aiProvider.generateEmbedding(text);
        return floatVec.stream().map(Float::doubleValue).toList();
    }

    private List<String> extractCategories(BookJsonDto dto) {
        List<String> cats = new ArrayList<>();
        if (dto.getCategory1() != null) cats.add(dto.getCategory1());
        if (dto.getCategory2() != null) cats.add(dto.getCategory2());
        if (dto.getCategory3() != null) cats.add(dto.getCategory3());
        return cats;
    }
}