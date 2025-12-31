package com.daisobook.shop.booksearch.BooksSearch.controller.external;

import com.daisobook.shop.booksearch.BooksSearch.controller.docs.BookManagementControllerDocs;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.meta.AdminBookMetaData;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.meta.FindIsbnMetaData;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.meta.ModifyBookMetaData;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.meta.RegisterBookMetaData;
import com.daisobook.shop.booksearch.BooksSearch.service.meta.MetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/books")
public class BookManagementController implements BookManagementControllerDocs {

    private final MetadataService metadataService;

    /**
     * 관리자 도서 목록 관리 페이지 메타데이터
     */
    @GetMapping("/admin")
    public AdminBookMetaData getBookAdminPageInfo(@PageableDefault(size = 15, sort = "publication_date", direction = Sort.Direction.DESC) Pageable pageable){
        return metadataService.getAdminBookMataData(pageable);
    }

    /**
     * 도서 등록을 위한 기초 데이터 조회
     */
    @GetMapping("/metadata/registration")
    public RegisterBookMetaData getBookRegisterPageInfo(){
        return metadataService.getRegisterBookMataDataFromAdmin();
    }

    /**
     * 특정 도서 수정을 위한 상세 메타데이터 조회
     */
    @GetMapping("/{bookId}/metadata/modification")
    public ModifyBookMetaData getBookModifyPageInfo(@PathVariable("bookId") long bookId){
        return metadataService.getModifyBookMataDataFromAdmin(bookId);
    }

    /**
     * ISBN 검색을 통한 등록 정보 조회
     */
    @GetMapping("{isbn}/register-page")
    public FindIsbnMetaData getBookRegisterRedirectSearchInfo(@PathVariable("isbn") String isbn){
        return metadataService.getFindIsbnMataDataFromAdmin(isbn);
    }

}
