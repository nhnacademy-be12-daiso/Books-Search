package com.daisobook.shop.booksearch.BooksSearch.controller.docs;

import com.daisobook.shop.booksearch.BooksSearch.dto.response.SortBookListRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.TotalDataRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.book.BookAdminResponseDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.book.BookRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.book.BookUpdateView;
import com.daisobook.shop.booksearch.BooksSearch.entity.BookListType;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Book V2 API", description = "도서 등록, 수정, 삭제 및 조회 기능을 제공하는 API (v2)")
public interface BookV2ControllerDocs {

    @Operation(summary = "단일 도서 등록", description = "메타데이터 JSON과 최대 5개의 이미지를 업로드하여 도서를 등록합니다.")
    @ApiResponse(responseCode = "201", description = "도서 등록 성공")
    ResponseEntity<Void> addBook(String metadataJson, MultipartFile image0, MultipartFile image1,
                                 MultipartFile image2, MultipartFile image3, MultipartFile image4) throws JsonProcessingException;

    @Operation(summary = "복수 도서 등록 (Batch)", description = "파일(CSV 등)을 업로드하여 여러 권의 도서를 한 번에 등록합니다.")
    @ApiResponse(responseCode = "201", description = "일괄 등록 요청 성공")
    ResponseEntity<Void> addBooks(MultipartFile bookFile) throws JsonProcessingException;

    @Operation(summary = "단일 도서 수정", description = "기존 도서의 정보 및 이미지를 수정합니다.")
    @ApiResponse(responseCode = "204", description = "도서 수정 성공 (반환 값 없음)")
    ResponseEntity<Void> motifyBook(long bookId, String metadataJson, MultipartFile image0, MultipartFile image1,
                                    MultipartFile image2, MultipartFile image3, MultipartFile image4) throws JsonProcessingException;

    @Operation(summary = "ID로 도서 삭제", description = "도서 고유 ID를 이용해 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "도서 삭제 성공")
    ResponseEntity<Void> deleteBookById(long bookId);

    @Operation(summary = "ISBN으로 도서 삭제", description = "도서 ISBN 번호를 이용해 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "도서 삭제 성공")
    ResponseEntity<Void> deleteBookByIsbn(String isbn);

    @Operation(summary = "리스트 타입별 도서 조회", description = "베스트셀러, 신간 등 타입별 도서 목록을 가져옵니다.")
    SortBookListRespDTO getBookListBySort(Pageable pageable, BookListType listType, Long userId);

    @Operation(summary = "도서 상세 정보 조회")
    BookRespDTO getBookDetail(long bookId, Long userId);

    @Operation(summary = "수정용 도서 데이터 조회", description = "수정 화면 구성을 위한 기존 데이터를 조회합니다.")
    BookUpdateView getBookUpdateView(long bookId);

    @Operation(summary = "관리자용 도서 전체 목록 조회")
    Page<BookAdminResponseDTO> findAllForAdmin(Pageable pageable);

    @Operation(summary = "관리자 통계 데이터 조회")
    TotalDataRespDTO getTotalData();

    @Operation(summary = "ISBN 중복 체크", description = "해당 ISBN이 이미 등록되어 있는지 확인합니다.")
    boolean getBookRegisterInfoByIsbn(String isbn);

    //    @Operation(summary = "ISBN 기반 도서 정보 정제 조회", description = "ISBN 정보를 바탕으로 외부 API 등에서 정제된 도서 정보를 가져옵니다.")
    //    BookInfoDataView postBookRegisterInfoByIsbn(String isbn);
}