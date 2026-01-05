package com.daisobook.shop.booksearch.dto.response.meta;

import com.daisobook.shop.booksearch.dto.response.RoleNameListRespDTO;
import com.daisobook.shop.booksearch.dto.response.category.CategoryList;

public record RegisterBookMetaData(
        CategoryList categoryList,
        RoleNameListRespDTO roleNameList
) {
}
