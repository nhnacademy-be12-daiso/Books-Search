package com.daisobook.shop.booksearch.service.point;

import com.daisobook.shop.booksearch.dto.point.PointPolicyType;

public interface PointService {
    void requestReviewPoint(long userId, PointPolicyType type);
}
