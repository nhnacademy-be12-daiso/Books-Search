package com.example.aladingemini;

import com.example.aladingemini.model.BookRecord;
import com.example.aladingemini.model.LlmResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

@Component
public class GeminiClient {

    private final GeminiProperties props;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public GeminiClient(GeminiProperties props, ObjectMapper objectMapper) {
        this.props = props;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public boolean isConfigured() {
        return props.getApiKey() != null && !props.getApiKey().isBlank();
    }

    public LlmResponse enrichBook(BookRecord raw) throws Exception {
        if (!isConfigured()) {
            throw new IllegalStateException("gemini.api-key 가 설정되지 않았습니다. 환경 변수 GEMINI_API_KEY 를 확인하세요.");
        }

        String prompt = buildPrompt(raw);

        // 요청 JSON 구성
        JsonNode root = objectMapper.createObjectNode();
        ArrayNode contents = ((com.fasterxml.jackson.databind.node.ObjectNode) root).putArray("contents");
        ObjectNode content0 = contents.addObject();
        ArrayNode parts = content0.putArray("parts");
        parts.addObject().put("text", prompt);
        ((com.fasterxml.jackson.databind.node.ObjectNode) root)
                .putObject("generationConfig")
                .put("response_mime_type", "application/json");

        String requestBody = objectMapper.writeValueAsString(root);

        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/"
                + props.getModel() + ":generateContent";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint + "?key=" + props.getApiKey()))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Gemini API 오류: HTTP " + response.statusCode()
                    + " body=" + response.body());
        }

        JsonNode responseRoot = objectMapper.readTree(response.body());
        JsonNode candidates = responseRoot.path("candidates");
        if (!candidates.isArray() || candidates.size() == 0) {
            throw new RuntimeException("Gemini 응답에 candidates 가 없습니다: " + response.body());
        }

        JsonNode content = candidates.get(0).path("content");
        JsonNode respParts = content.path("parts");
        if (!respParts.isArray() || respParts.size() == 0) {
            throw new RuntimeException("Gemini 응답에 parts 가 없습니다: " + response.body());
        }

        String jsonText = respParts.get(0).path("text").asText();
        if (jsonText == null || jsonText.isBlank()) {
            throw new RuntimeException("Gemini 응답의 text 가 비어 있습니다: " + response.body());
        }

        JsonNode jsonNode = objectMapper.readTree(jsonText);

        LlmResponse result = new LlmResponse();
        result.setDescription(jsonNode.path("description").asText(null));

        if (jsonNode.has("toc") && jsonNode.get("toc").isArray()) {
            result.setToc(objectMapper.convertValue(
                    jsonNode.get("toc"),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            ));
        }

        result.setCategoryPath(jsonNode.path("category_path").asText(null));

        if (jsonNode.has("tags") && jsonNode.get("tags").isArray()) {
            result.setTags(objectMapper.convertValue(
                    jsonNode.get("tags"),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            ));
        }

        if (jsonNode.has("packable")) {
            result.setPackable(jsonNode.get("packable").asBoolean());
        }

        return result;
    }

    private String buildPrompt(BookRecord raw) {
        StringBuilder sb = new StringBuilder();
        sb.append("당신은 도서 메타데이터를 생성하는 AI입니다.\n\n")
          .append("아래 도서 정보를 기반으로, 다음 항목을 반드시 JSON 형식으로 생성하세요.\n")
          .append("1) \"description\": 이 책의 내용을 설명하는 한국어 상세 설명 (300~600자)\n")
          .append("2) \"toc\": 현실감 있는 목차 리스트 (문자열 배열, 최대 15개 항목)\n")
          .append("3) \"category_path\": 아래 카테고리 트리 중에서 가장 적절한 경로 1개\n")
          .append("4) \"tags\": 이 책을 설명하는 한국어 태그 5~10개 (각 태그는 1~3 단어)\n")
          .append("5) \"packable\": 선물용 포장이 적절한 책이면 true, 아니면 false\n\n")
          .append("--- 도서 원본 정보 ---\n")
          .append("제목: ").append(nullSafe(raw.getTitle())).append("\n")
          .append("저자: ").append(nullSafe(raw.getAuthor())).append("\n")
          .append("출판사: ").append(nullSafe(raw.getPublisher())).append("\n")
          .append("출간일: ").append(nullSafe(raw.getPubDate())).append("\n")
          .append("정가: ").append(raw.getPriceStandard() == null ? "" : raw.getPriceStandard()).append("\n")
          .append("알라딘 카테고리: ").append(nullSafe(raw.getAladinCategoryName())).append("\n")
          .append("알라딘 설명: ").append(nullSafe(raw.getDescription())).append("\n\n")
          .append("--- 카테고리 트리 ---\n")
          .append("소설\n")
          .append("  - 한국소설\n")
          .append("  - 일본소설\n")
          .append("  - 영미소설\n")
          .append("  - 장르소설(판타지/추리/로맨스)\n")
          .append("IT/프로그래밍\n")
          .append("  - 언어/프레임워크\n")
          .append("  - 인공지능/머신러닝\n")
          .append("  - 데이터베이스\n")
          .append("인문/사회\n")
          .append("  - 심리학\n")
          .append("  - 사회학\n")
          .append("경제/경영\n")
          .append("자기계발\n")
          .append("에세이\n")
          .append("어린이/청소년\n\n")
          .append("출력은 아래 JSON 형식만 사용하세요. 설명은 모두 한국어여야 합니다.\n")
          .append("{\n")
          .append("  \"description\": \"...\",\n")
          .append("  \"toc\": [\"...\", \"...\"],\n")
          .append("  \"category_path\": \"소설 > 한국소설\",\n")
          .append("  \"tags\": [\"...\", \"...\"],\n")
          .append("  \"packable\": true\n")
          .append("}\n");

        return sb.toString();
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }
}
