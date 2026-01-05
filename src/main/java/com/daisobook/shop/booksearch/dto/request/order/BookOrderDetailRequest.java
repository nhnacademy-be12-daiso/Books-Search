package com.daisobook.shop.booksearch.dto.request.order;

public record BookOrderDetailRequest (
    long bookId,
    long orderDetailId){
}