package com.daisobook.shop.booksearch.books_search.dto.response.meta;

import com.daisobook.shop.booksearch.books_search.dto.response.RoleNameListRespDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.book.BookUpdateView;
import com.daisobook.shop.booksearch.books_search.dto.response.category.CategoryList;

public record ModifyBookMetaData(
        BookUpdateView bookUpdateView,
        CategoryList categoryList,
        RoleNameListRespDTO roleNameList
) {
}
