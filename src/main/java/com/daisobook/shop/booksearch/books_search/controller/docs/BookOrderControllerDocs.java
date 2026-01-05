package com.daisobook.shop.booksearch.books_search.controller.docs;

import com.daisobook.shop.booksearch.books_search.dto.request.BookIdListReqDTO;
import com.daisobook.shop.booksearch.books_search.dto.request.order.BookReviewRequest;
import com.daisobook.shop.booksearch.books_search.dto.request.order.OrderCancelRequest;
import com.daisobook.shop.booksearch.books_search.dto.response.order.BookReviewResponse;
import com.daisobook.shop.booksearch.books_search.dto.response.order.OrderBookSummeryDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.order.OrderBooksInfoRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Book Order", description = "주문 서비스 연동용 도서 정보 조회 API")
public interface BookOrderControllerDocs {

    @Operation(summary = "주문 도서 상세 정보 조회", description = "주문 처리에 필요한 도서들의 상세 정보를 리스트로 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 도서 ID 리스트")
    })
    OrderBooksInfoRespDTO getOrderBookInfoList(
        @Parameter(description = "도서 ID 리스트", required = true) BookIdListReqDTO bookIdListReqDTO
    );

    @Operation(summary = "주문 도서 요약 목록 조회", description = "장바구니나 주문 목록 표시를 위한 도서 요약 정보를 조회합니다.")
    List<OrderBookSummeryDTO> getBookList(
        @Parameter(description = "도서 ID 리스트", required = true) BookIdListReqDTO bookIdListReqDTO
    );

    @Operation(summary = "사용자 도서 리뷰 상태 조회", description = "주문한 도서들에 대해 사용자가 리뷰를 작성했는지 여부를 포함한 목록을 조회합니다.")
    List<BookReviewResponse> getBookReviewList(
        @Parameter(description = "사용자 ID 및 도서 상세 정보", required = true) BookReviewRequest bookReviewRequest
    );

    @Operation(summary = "주문 취소 시 재고 및 데이터 복구", description = "주문이 취소되었을 때 해당 도서들의 상태를 업데이트하거나 관련 처리를 수행합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "취소 처리 성공"),
            @ApiResponse(responseCode = "404", description = "주문 정보를 찾을 수 없음")
    })
    ResponseEntity<Void> orderCancel(
            @Parameter(description = "취소할 주문 및 도서 정보", required = true) OrderCancelRequest orderCancelRequest
    );
}