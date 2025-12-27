package com.daisobook.shop.booksearch.BooksSearch.dto.response.meta;

import com.daisobook.shop.booksearch.BooksSearch.dto.response.RoleNameListRespDTO;
import com.daisobook.shop.booksearch.BooksSearch.dto.response.category.CategoryList;

public record RegisterBookMetaData(
        CategoryList categoryList,
        RoleNameListRespDTO roleNameList
) {
}
