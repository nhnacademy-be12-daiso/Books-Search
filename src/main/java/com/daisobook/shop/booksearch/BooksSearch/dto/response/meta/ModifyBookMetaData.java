package com.daisobook.shop.booksearch.BooksSearch.dto.response.meta;

import com.daisobook.shop.booksearch.BooksSearch.dto.response.RoleNameListRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.book.BookUpdateView;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.category.CategoryList;

public record ModifyBookMetaData(
        BookUpdateView bookUpdateView,
        CategoryList categoryList,
        RoleNameListRespDTO roleNameList
) {
}
