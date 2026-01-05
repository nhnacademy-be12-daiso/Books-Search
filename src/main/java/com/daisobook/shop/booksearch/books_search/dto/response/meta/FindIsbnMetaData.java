package com.daisobook.shop.booksearch.books_search.dto.response.meta;

import com.daisobook.shop.booksearch.books_search.dto.api.BookInfoDataView;
import com.daisobook.shop.booksearch.books_search.dto.response.RoleNameListRespDTO;
import com.daisobook.shop.booksearch.books_search.dto.response.category.CategoryList;

public record FindIsbnMetaData(
        BookInfoDataView refinedBook,
        CategoryList categoryList,
        RoleNameListRespDTO roleNameList
) {
}
