package com.daisobook.shop.booksearch.dto.response.meta;

import com.daisobook.shop.booksearch.dto.response.RoleNameListRespDTO;
import com.daisobook.shop.booksearch.dto.response.book.BookUpdateView;
import com.daisobook.shop.booksearch.dto.response.category.CategoryList;

public record ModifyBookMetaData(
        BookUpdateView bookUpdateView,
        CategoryList categoryList,
        RoleNameListRespDTO roleNameList
) {
}
