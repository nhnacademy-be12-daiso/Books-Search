package com.daisobook.shop.booksearch.dto.response.like;

import java.util.List;

public record MyLikeList (
        List<LikeListRespDTO> likeList
) {
}
