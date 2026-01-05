package com.daisobook.shop.booksearch.books_search.dto.response.like;

import java.util.List;

public record MyLikeList (
        List<LikeListRespDTO> likeList
) {
}
