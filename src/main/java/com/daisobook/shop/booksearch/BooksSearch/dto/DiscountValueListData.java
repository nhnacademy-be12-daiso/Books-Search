package com.daisobook.shop.booksearch.BooksSearch.dto;

import com.daisobook.shop.booksearch.BooksSearch.entity.policy.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class DiscountValueListData {
    long id;
    String name;
    DiscountType discountType;
    Double value;
}
