package com.daisobook.shop.booksearch.BooksSearch.service.meta;

import com.daisobook.shop.booksearch.BooksSearch.dto.api.BookInfoDataView;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.RoleNameListRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.SortBookListRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.TotalDataRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.book.BookAdminResponseDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.book.BookUpdateView;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.book.MainPageBookListRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.category.CategoryList;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.meta.AdminBookMetaData;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.meta.FindIsbnMetaData;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.meta.ModifyBookMetaData;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.meta.RegisterBookMetaData;
import com.daisobook.shop.booksearch.BooksSearch.entity.BookListType;
import com.daisobook.shop.booksearch.BooksSearch.service.api.BookRefineService;
import com.daisobook.shop.booksearch.BooksSearch.service.author.AuthorV2Service;
import com.daisobook.shop.booksearch.BooksSearch.service.book.impl.BookFacade;
import com.daisobook.shop.booksearch.BooksSearch.service.category.CategoryV2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

@Slf4j
@RequiredArgsConstructor
@Service
public class MetadataService {
    private final BookRefineService bookRefineService;
    private final CategoryV2Service categoryService;
    private final AuthorV2Service authorService;
    private final BookFacade bookFacade;

    public AdminBookMetaData getAdminBookMataData(Pageable pageable){
        Page<BookAdminResponseDTO> bookAdminResponseDTOS = bookFacade.findAllForAdmin(pageable);
        TotalDataRespDTO totalDate = bookFacade.getTotalDate();

        return new AdminBookMetaData(bookAdminResponseDTOS, totalDate);
    }

    public RegisterBookMetaData getRegisterBookMataDataFromAdmin(){
        CategoryList categoryList = categoryService.getCategoryList();
        RoleNameListRespDTO roleNameList = authorService.getRoleNameList();

        return new RegisterBookMetaData(categoryList, roleNameList);
    }

    public ModifyBookMetaData getModifyBookMataDataFromAdmin(long bookId){
        BookUpdateView bookUpdateView = bookFacade.getBookUpdateView(bookId);
        CategoryList categoryList = categoryService.getCategoryList();
        RoleNameListRespDTO roleNameList = authorService.getRoleNameList();

        return new ModifyBookMetaData(bookUpdateView, categoryList, roleNameList);
    }

    public FindIsbnMetaData getFindIsbnMataDataFromAdmin(String isbn){
        BookInfoDataView refinedBook = bookRefineService.getRefinedBook(isbn);
        CategoryList categoryList = categoryService.getCategoryList();
        RoleNameListRespDTO roleNameList = authorService.getRoleNameList();

        return new FindIsbnMetaData(refinedBook, categoryList, roleNameList);
    }

    public MainPageBookListRespDTO getMainPageBookList(Pageable pageable, Long userId){
        if(pageable == null) {
            pageable = PageRequest.of(0, 15);
        }
        SortBookListRespDTO list1 = bookFacade.getBookList(pageable, BookListType.BOOK_OF_THE_WEEK, userId);
        SortBookListRespDTO list2 = bookFacade.getBookList(pageable, BookListType.NEW_RELEASES, userId);
        return new MainPageBookListRespDTO(list1, list2);
    }
}
