package com.daisobook.shop.booksearch.BooksSearch.search.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.ScrollResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.daisobook.shop.booksearch.BooksSearch.search.domain.Book;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Repository("BookSearchRepository")
@RequiredArgsConstructor
public class BookRepository {

    private final ElasticsearchClient esClient;
    private static final String INDEX_NAME = "books";

    // 1. ISBN 검색
    public List<Book> findByIsbn(String isbn) {
        try {
            SearchResponse<Book> response = esClient.search(s -> s
                    .index(INDEX_NAME)
                    .query(q -> q.term(t -> t.field("isbn").value(isbn))), Book.class);
            return extractHits(response);
        } catch (IOException e) {
            log.error("ISBN 조회 중 오류 발생: {}", isbn, e);
            return Collections.emptyList();
        }
    }

    // 2. 하이브리드 검색 (Vector + Keyword)
    public List<Book> searchHybrid(String query, List<Float> vector, int size) {
        try {
            SearchResponse<Book> response = esClient.search(s -> s
                    .index(INDEX_NAME)
                    .knn(k -> k
                            .field("embedding")
                            .queryVector(vector)
                            .k(size)
                            .numCandidates(100)
                            .boost(10.0f))
                    .query(q -> q.bool(b -> b.should(m -> m.multiMatch(mm -> mm
                            .query(query)
                            .fields("title^3", "categories^3", "author^1", "description^1")
                            .analyzer("korean_analyzer")
                            .minimumShouldMatch("2<75%")))))
                    .source(src -> src.filter(f -> f.excludes("embedding"))), Book.class);
            return extractHits(response);
        } catch (IOException e) {
            log.error("하이브리드 검색 오류", e);
            return Collections.emptyList();
        }
    }

    // 3. 단건 저장
    public void save(Book book) {
        try {
            esClient.index(i -> i
                    .index(INDEX_NAME)
                    .id(book.getIsbn())
                    .document(book)
                    // 🔥 [핵심] 저장 즉시 검색 결과에 반영되도록 설정 (테스트 시 필수)
                    .refresh(co.elastic.clients.elasticsearch._types.Refresh.True)
            );
        } catch (IOException e) {
            // 🔥 [핵심] 에러를 삼키지 않고 던져서 Service -> Controller -> GlobalExceptionHandler로 전달
            throw new RuntimeException("Elasticsearch 저장 실패: " + e.getMessage(), e);
        }
    }

    // 4. Bulk 저장
    public void saveAll(List<Book> books) {
        if (books.isEmpty()) return;
        try {
            BulkRequest.Builder br = new BulkRequest.Builder();
            for (Book book : books) {
                br.operations(op -> op.index(idx -> idx.index(INDEX_NAME).id(book.getIsbn()).document(book)));
            }
            esClient.bulk(br.build());
        } catch (IOException e) {
            log.error("Bulk Insert 실패", e);
            // 대량 등록은 보통 로그만 찍고 넘어가거나, 필요 시 예외를 던질 수 있음
        }
    }

    // 5. 전체 ISBN 가져오기 (Scroll API)
    public Set<String> findAllIsbns() {
        Set<String> isbns = new HashSet<>();
        String scrollId = null;
        try {
            SearchResponse<Void> response = esClient.search(s -> s
                    .index(INDEX_NAME)
                    .size(5000)
                    .scroll(t -> t.time("1m"))
                    .source(src -> src.fetch(false)), Void.class);

            scrollId = response.scrollId();
            if (response.hits() != null) response.hits().hits().forEach(hit -> isbns.add(hit.id()));

            while (scrollId != null) {
                String finalScrollId = scrollId;
                ScrollResponse<Void> scrollResponse = esClient.scroll(s -> s.scrollId(finalScrollId).scroll(t -> t.time("1m")), Void.class);
                if (scrollResponse.hits() == null || scrollResponse.hits().hits().isEmpty()) break;
                scrollResponse.hits().hits().forEach(hit -> isbns.add(hit.id()));
                scrollId = scrollResponse.scrollId();
            }
        } catch (Exception e) {
            log.warn("기존 ISBN 조회 실패 (첫 실행 시 무시): {}", e.getMessage());
        } finally {
            if (scrollId != null) {
                String sid = scrollId;
                try { esClient.clearScroll(c -> c.scrollId(sid)); } catch (Exception ignored) {}
            }
        }
        return isbns;
    }

    private List<Book> extractHits(SearchResponse<Book> response) {
        return response.hits().hits().stream().map(Hit::source).collect(Collectors.toList());
    }
}