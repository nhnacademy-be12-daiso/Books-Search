package com.daisobook.shop.booksearch.dto.response.meta;

import com.daisobook.shop.booksearch.dto.api.BookInfoDataView;
import com.daisobook.shop.booksearch.dto.response.RoleNameListRespDTO;
import com.daisobook.shop.booksearch.dto.response.category.CategoryList;

public record FindIsbnMetaData(
        BookInfoDataView refinedBook,
        CategoryList categoryList,
        RoleNameListRespDTO roleNameList
) {
}
