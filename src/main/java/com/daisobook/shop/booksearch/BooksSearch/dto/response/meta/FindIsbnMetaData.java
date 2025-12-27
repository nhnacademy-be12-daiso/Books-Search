package com.daisobook.shop.booksearch.BooksSearch.dto.response.meta;

import com.daisobook.shop.booksearch.BooksSearch.dto.api.BookInfoDataView;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.RoleNameListRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.category.CategoryList;

public record FindIsbnMetaData(
        BookInfoDataView refinedBook,
        CategoryList categoryList,
        RoleNameListRespDTO roleNameList
) {
}
