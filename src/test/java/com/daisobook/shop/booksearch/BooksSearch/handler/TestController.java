package com.daisobook.shop.booksearch.BooksSearch.handler;

import com.daisobook.shop.booksearch.BooksSearch.exception.custom.DuplicateResourceException;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.EntityNotFoundException;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.InvalidRequestException;
import com.daisobook.shop.booksearch.BooksSearch.exception.custom.saga.ExternalServiceException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/test/not-found")
    public void throwNotFound() { throw new EntityNotFoundException("대상 없음"); }

    @GetMapping("/test/conflict")
    public void throwConflict() { throw new DuplicateResourceException("중복 발생"); }

    @GetMapping("/test/bad-request")
    public void throwBadRequest() { throw new InvalidRequestException("잘못된 요청"); }

    @GetMapping("/test/internal-error")
    public void throwInternal() { throw new ExternalServiceException("외부 서비스 에러"); }

    @GetMapping("/test/unexpected")
    public void throwUnexpected() { throw new RuntimeException("알 수 없는 에러"); }
}