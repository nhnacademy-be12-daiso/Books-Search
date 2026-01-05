package com.daisobook.shop.booksearch.books_search.exception;

import com.daisobook.shop.booksearch.books_search.exception.custom.BusinessException;
import com.daisobook.shop.booksearch.books_search.exception.custom.DuplicateResourceException;
import com.daisobook.shop.booksearch.books_search.exception.custom.EntityNotFoundException;
import com.daisobook.shop.booksearch.books_search.exception.custom.InvalidRequestException;
import com.daisobook.shop.booksearch.books_search.exception.custom.ai.BookNotFoundException;
import com.daisobook.shop.booksearch.books_search.exception.custom.ai.GeminiQuotaException;
import com.daisobook.shop.booksearch.books_search.exception.custom.ai.LlmAnalysisException;
import com.daisobook.shop.booksearch.books_search.exception.custom.image.ImageServiceException;
import com.daisobook.shop.booksearch.books_search.exception.custom.mapper.FailObjectMapper;
import com.daisobook.shop.booksearch.books_search.exception.custom.saga.BookOutOfStockException;
import com.daisobook.shop.booksearch.books_search.exception.custom.saga.ExternalServiceException;
import com.daisobook.shop.booksearch.books_search.exception.custom.saga.FailedSerializationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler {

    /**
     * [404 Not Found] 존재하지 않는 리소스 접근
     */
    @ExceptionHandler({
            EntityNotFoundException.class,
            BookNotFoundException.class // AI 도메인
    })
    public ResponseEntity<ErrorResponse> handleNotFound(BusinessException ex) {
        log.warn("Resource Not Found: {}", ex.getMessage());
        return buildErrorResponse(ex, ex.getHttpStatus(), ex.getMessage());
    }

    /**
     * [409 Conflict] 이미 존재하는 데이터 (중복)
     */
    @ExceptionHandler({
            DuplicateResourceException.class
    })
    public ResponseEntity<ErrorResponse> handleConflict(BusinessException ex) {
        log.warn("Resource Conflict: {}", ex.getMessage());
        return buildErrorResponse(ex, ex.getHttpStatus(), ex.getMessage());
    }

    /**
     * [400 Bad Request] 비즈니스 로직상 잘못된 요청 또는 변경 불가
     */
    @ExceptionHandler({
            InvalidRequestException.class,
            BookOutOfStockException.class // SAGA/Inventory 관련
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(BusinessException ex) {
        log.warn("Bad Request: {}", ex.getMessage());
        return buildErrorResponse(ex, ex.getHttpStatus(), ex.getMessage());
    }

    /**
     * [500 Internal Server Error] 시스템 내부 오류 및 외부 서비스 연동 실패
     */
    @ExceptionHandler({
            FailObjectMapper.class,
            FailedSerializationException.class,
            ExternalServiceException.class,
            LlmAnalysisException.class,
            GeminiQuotaException.class, // 쿼터 초과는 429로 보낼 수도 있지만 보통 500군으로 처리
            ImageServiceException.class
    })
    public ResponseEntity<ErrorResponse> handleInternalServerError(BusinessException ex) {
        log.error("System Error: ", ex);
        return buildErrorResponse(ex, ex.getHttpStatus() != null ? ex.getHttpStatus() : HttpStatus.INTERNAL_SERVER_ERROR,
                "시스템 내부 오류가 발생했습니다.");
    }

    /**
     * 예상치 못한 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex) {
        log.error("Unexpected Error: ", ex);
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 오류가 발생했습니다.");
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(Exception ex, HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ErrorResponse.create(ex, status, message));
    }
}
