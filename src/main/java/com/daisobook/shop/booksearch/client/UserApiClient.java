package com.daisobook.shop.booksearch.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="TEAM3-USER", path="/api/internal/points")
public interface UserApiClient {
    @PostMapping("/policy")
    ResponseEntity<Void> earnPointByPolicy(@RequestHeader("X-User-Id") Long userCreatedId,
                                           @RequestParam("policy-type") String policyType);

}