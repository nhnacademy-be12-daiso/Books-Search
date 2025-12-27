package com.daisobook.shop.booksearch.BooksSearch.controller.external;

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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BookManagementController {

    private final MetadataService metadataService;

    @GetMapping("/api/v2/books/book-admin-page")
    public AdminBookMetaData getBookAdminPageInfo(@PageableDefault(size = 15, sort = "publication_date", direction = Sort.Direction.DESC) Pageable pageable){
        return metadataService.getAdminBookMataData(pageable);
    }

    @GetMapping("/api/v2/books/register-page")
    public RegisterBookMetaData getBookRegisterPageInfo(){
        return metadataService.getRegisterBookMataDataFromAdmin();
    }

    @GetMapping("/api/v2/books/{bookId}/modify-page")
    public ModifyBookMetaData getBookModifyPageInfo(@PathVariable("bookId") long bookId){
        return metadataService.getModifyBookMataDataFromAdmin(bookId);
    }

    @GetMapping("/api/v2/books/{isbn}/register-page")
    public FindIsbnMetaData getBookRegisterRedirectSearchInfo(@PathVariable("isbn") String isbn){
        return metadataService.getFindIsbnMataDataFromAdmin(isbn);
    }

}
