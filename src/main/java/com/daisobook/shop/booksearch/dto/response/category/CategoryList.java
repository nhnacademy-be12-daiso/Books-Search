package com.daisobook.shop.booksearch.dto.response.category;

import java.util.List;

public record CategoryList(
        List<CategoryPath> categoryPathList
){
}
