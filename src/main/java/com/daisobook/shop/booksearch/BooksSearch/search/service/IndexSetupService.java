package com.daisobook.shop.booksearch.BooksSearch.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.StringReader;

/**
 * Elasticsearch 인덱스 설정 및 생성 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IndexSetupService {

    private final ElasticsearchClient esClient;

    private static final String SETTINGS_JSON = """
            {
              "settings": {
                "analysis": {
                  "filter": {
                    "my_synonyms_filter": {
                      "type": "synonym",
                      "synonyms": [
                        "아기, 유아",
                        "학생, 제자",
                        "구입, 구매",
                        "예쁜, 아름다운",
                        "슬픈, 우울한",
                        "기질, 특성",
                        "LA, 로스엔젤레스"
                      ]
                    }
                  },
                  "analyzer": {
                    "korean_analyzer": {
                      "type": "custom",
                      "tokenizer": "nori_tokenizer",
                      "filter": [
                        "my_synonyms_filter"
                      ]
                    }
                  }
                }
              },
              "mappings": {
                "properties": {
                  "title": {
                    "type": "text",
                    "analyzer": "korean_analyzer"
                  },
                  "author": {
                    "type": "keyword"
                  },
                  "tags": {
                    "type": "keyword"
                  },
                  "isbn": {
                    "type": "keyword"
                  },
                  "publisher": {
                    "type": "keyword"
                  },
                  "description": {
                    "type": "text",
                    "analyzer": "korean_analyzer"
                  },
                  "review_content": {
                    "type": "text",
                    "analyzer": "korean_analyzer"
                  },
                  "price": {
                    "type": "integer"
                  },
                  "pubDate": {
                    "type": "date"
                  },
                  "click_count": {
                    "type": "integer"
                  },
                  "rating": {
                    "type": "float"
                  },
                  "review_count": {
                    "type": "integer"
                  },
                  "categories": {
                    "type": "keyword"
                  },
                  "image_url": {
                    "type": "keyword",
                    "index": false,
                    "doc_values": false
                  },
                  "book_vector": {
                    "type": "dense_vector",
                    "dims": 1024,
                    "index": true,
                    "similarity": "cosine"
                  }
                }
              }
            }
            """;

    public void createIndex() {
        try {
            esClient.indices().create(new CreateIndexRequest.Builder()
                    .index("books")
                    .withJson(new StringReader(SETTINGS_JSON))
                    .build());
            log.info("인덱스 'books' 생성 완료");
        } catch (Exception e) {
            // 이미 존재하면 무시
        }
    }
}