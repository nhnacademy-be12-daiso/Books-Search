package com.daisobook.shop.booksearch.BooksSearch.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "book-open-api", url = "")
public interface AClient {
}
