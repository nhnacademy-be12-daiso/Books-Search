package com.daisobook.shop.booksearch.BooksSearch.service.point;

import com.daisobook.shop.booksearch.BooksSearch.dto.point.PointPolicyType;

public interface PointService {
    void requestReviewPoint(long userId, PointPolicyType type);
}
