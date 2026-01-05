package com.daisobook.shop.booksearch.books_search.service.point;

import com.daisobook.shop.booksearch.books_search.dto.point.PointPolicyType;

public interface PointService {
    void requestReviewPoint(long userId, PointPolicyType type);
}
