package com.bank.batch.client;


import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class AccountFallback implements AccountFeignClient {

    @Override
    public Map<String, Object> getAccount(String accountNumber) {
        throw new RuntimeException("Account service unavailable");
    }
}
