package com.daisobook.shop.booksearch.BooksSearch.adapter;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value="gateway")
public interface MemberAdapter {
    @GetMapping("/{id}")
    /*Optional<Member>*/void getMember(@PathVariable("id") String id);

}