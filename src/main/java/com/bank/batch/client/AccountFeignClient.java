package com.bank.batch.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(
        name = "account-service",
        url = "${account.service.base-url}",
        fallback = AccountFallback.class
)
public interface AccountFeignClient {

    @GetMapping("/{accountNumber}")
    Map<String, Object> getAccount(@PathVariable String accountNumber);
}
