package com.bank.batch.client;

import com.bank.batch.exception.AccountServiceUnavailableException;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AccountFallback implements AccountFeignClient {

    @Override
    public Map<String, Object> getAccount(String accountNumber) {
        throw new AccountServiceUnavailableException("Account not found or account-service unavailable for accountNumber={}" + accountNumber);
    }
}
