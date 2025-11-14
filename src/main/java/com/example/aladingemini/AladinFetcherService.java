package com.example.aladingemini;

import com.example.aladingemini.model.BookRecord;
import com.example.aladingemini.model.SaleStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AladinFetcherService {

    private static final String BASE_URL = "https://www.aladin.co.kr/ttb/api/ItemList.aspx";

    private final AladinProperties props;
    private final ObjectMapper mapper;
    private final HttpClient httpClient;

    public AladinFetcherService(AladinProperties props, ObjectMapper mapper) {
        this.props = props;
        this.mapper = mapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public List<BookRecord> fetchBooks() throws Exception {
        if (props.getTtbKey() == null || props.getTtbKey().isBlank()) {
            throw new IllegalStateException("aladin.ttb-key 가 설정되지 않았습니다. 환경 변수 ALADIN_TTB_KEY 를 확인하세요.");
        }

        List<Integer> categoryIds = props.categoryIdList();
        if (categoryIds.isEmpty()) {
            throw new IllegalStateException("aladin.category-ids 가 비어 있습니다. application.yml 을 확인하세요.");
        }

        Set<String> seenIsbn13 = new HashSet<>();
        List<BookRecord> result = new ArrayList<>();

        int callCount = 0;
        int maxCalls = props.getMaxApiCallsPerDay();

        outer:
        for (Integer categoryId : categoryIds) {
            for (int page = 1; page <= props.getMaxPagesPerCategory(); page++) {

                if (callCount >= maxCalls) {
                    System.out.println("알라딘 API 호출 상한에 도달했습니다. 수집을 중단합니다.");
                    break outer;
                }
                if (result.size() >= props.getTargetItemCount()) {
                    System.out.println("목표 도서 수(" + props.getTargetItemCount() + ")를 달성했습니다. 수집을 중단합니다.");
                    break outer;
                }

                String url = buildUrl(props.getTtbKey(), categoryId, page, 100);
                System.out.println("Aladin Request: " + url);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(30))
                        .GET()
                        .build();

                HttpResponse<String> response =
                        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                callCount++;

                if (response.statusCode() != 200) {
                    System.err.println("알라딘 HTTP 오류: " + response.statusCode());
                    continue;
                }

                JsonNode root = mapper.readTree(response.body());
                JsonNode itemsNode = root.get("item");

                if (itemsNode == null || !itemsNode.isArray() || itemsNode.size() == 0) {
                    System.out.println("CategoryId=" + categoryId + ", page=" + page + " 결과 없음. 다음 카테고리로.");
                    break;
                }

                for (JsonNode itemNode : itemsNode) {
                    String isbn13 = itemNode.path("isbn13").asText();
                    if (isbn13 == null || isbn13.isBlank()) {
                        continue;
                    }

                    if (!seenIsbn13.add(isbn13)) {
                        // 중복 도서
                        continue;
                    }

                    BookRecord book = new BookRecord();
                    book.setIsbn13(isbn13);
                    book.setTitle(itemNode.path("title").asText());
                    book.setAuthor(itemNode.path("author").asText());
                    book.setPublisher(itemNode.path("publisher").asText());
                    book.setDescription(itemNode.path("description").asText());
                    book.setPubDate(itemNode.path("pubDate").asText());
                    if (itemNode.hasNonNull("priceStandard")) {
                        book.setPriceStandard(itemNode.get("priceStandard").asInt());
                    }
                    book.setAladinCategoryName(itemNode.path("categoryName").asText());

                    // 재고 1~10 랜덤
                    book.setStock(ThreadLocalRandom.current().nextInt(1, 11));
                    // 상태: 판매중
                    book.setSaleStatus(SaleStatus.ON_SALE);

                    result.add(book);

                    if (result.size() >= props.getTargetItemCount()) {
                        break outer;
                    }
                }
            }
        }

        System.out.println("알라딘 수집 완료. 총 도서 수(중복 제거 후): " + result.size());
        System.out.println("알라딘 API 호출 수: " + callCount);

        return result;
    }

    private String buildUrl(String ttbKey, int categoryId, int startPage, int maxResults) {
        String encodedKey = URLEncoder.encode(ttbKey, StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder(BASE_URL);
        sb.append("?ttbkey=").append(encodedKey);
        sb.append("&QueryType=ItemNewAll");
        sb.append("&SearchTarget=Book");
        sb.append("&output=js");
        sb.append("&Version=20131101");
        sb.append("&MaxResults=").append(maxResults);
        sb.append("&start=").append(startPage);
        sb.append("&CategoryId=").append(categoryId);
        return sb.toString();
    }
}
