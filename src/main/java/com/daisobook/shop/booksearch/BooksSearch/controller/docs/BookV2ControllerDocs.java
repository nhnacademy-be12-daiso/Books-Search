package com.daisobook.shop.booksearch.BooksSearch.controller.docs;

import com.daisobook.shop.booksearch.BooksSearch.dto.api.BookInfoDataView;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.SortBookListRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.TotalDataRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.book.BookAdminResponseDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.book.BookRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.book.BookUpdateView;
import com.daisobook.shop.booksearch.BooksSearch.entity.BookListType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "도서 V2 API", description = "도서 등록, 수정, 삭제 및 조회 기능을 제공하는 API (v2)")
public interface BookV2ControllerDocs {

    @Operation(summary = "단일 도서 등록", description = "메타데이터 JSON과 최대 5개의 이미지를 업로드하여 도서를 등록합니다.")
    @ApiResponse(responseCode = "200", description = "도서 등록 성공")
    ResponseEntity addBook(
            @Parameter(description = "도서 정보 JSON (String)", required = true) String metadataJson,
            MultipartFile image0, MultipartFile image1, MultipartFile image2, MultipartFile image3, MultipartFile image4) throws Exception;

    @Operation(summary = "복수 도서 등록 (Batch)", description = "CSV 등 파일을 업로드하여 여러 권의 도서를 한 번에 등록합니다.")
    ResponseEntity addBooks(MultipartFile bookFile) throws Exception;

    @Operation(summary = "단일 도서 수정", description = "기존 도서의 정보 및 이미지를 수정합니다.")
    ResponseEntity motifyBook(
            @Parameter(description = "수정할 도서 ID", required = true) long bookId,
            @Parameter(description = "수정할 메타데이터 JSON", required = true) String metadataJson,
            MultipartFile image0, MultipartFile image1, MultipartFile image2, MultipartFile image3, MultipartFile image4) throws Exception;

    @Operation(summary = "ID로 도서 삭제", description = "도서의 고유 ID를 이용해 해당 도서를 삭제합니다.")
    ResponseEntity deleteBookById(@Parameter(description = "도서 ID") long bookId);

    @Operation(summary = "ISBN으로 도서 삭제", description = "도서의 ISBN 번호를 이용해 해당 도서를 삭제합니다.")
    ResponseEntity deleteBookByIsbn(@Parameter(description = "도서 ISBN") String isbn);

    @Operation(summary = "리스트 타입별 도서 조회", description = "베스트셀러, 신간 등 정렬 타입에 따른 도서 목록을 가져옵니다.")
    SortBookListRespDTO getBookListBySort(
            @Parameter(description = "리스트 타입 (BESTSELLER, NEW 등)") BookListType listType,
            @Parameter(description = "사용자 ID (비로그인 시 생략 가능)") Long userId);

    @Operation(summary = "도서 상세 정보 조회", description = "도서 ID를 통해 상세 정보를 조회합니다.")
    BookRespDTO getBookDetail(long bookId, Long userId);

    @Operation(summary = "수정용 도서 데이터 조회", description = "수정 화면을 구성하기 위한 기존 도서 데이터를 조회합니다.")
    BookUpdateView getBookUpdateView(long bookId);

    @Operation(summary = "관리자용 도서 전체 목록 조회", description = "관리자 페이지에서 사용되는 전체 도서 목록을 페이지네이션하여 조회합니다.")
    Page<BookAdminResponseDTO> findAllForAdmin(Pageable pageable);

    @Operation(summary = "관리자 통계 데이터 조회", description = "도서 관리 페이지 상단에 필요한 통계 정보를 조회합니다.")
    TotalDataRespDTO getTotalData();

    @Operation(summary = "ISBN 중복 체크", description = "AI 도서 등록 전, 해당 ISBN이 이미 등록되어 있는지 확인합니다.")
    boolean getBookRegisterInfoByIsbn(String isbn);

    @Operation(summary = "ISBN 기반 도서 정보 정제 조회", description = "ISBN 정보를 바탕으로 외부 API 등에서 정제된 도서 정보를 가져옵니다.")
    BookInfoDataView postBookRegisterInfoByIsbn(String isbn);
}