package com.bank.batch.processor;

import com.bank.batch.client.AccountFeignClient;
import com.bank.batch.dto.ApiResponse;
import com.bank.batch.entity.Account;
import com.bank.batch.entity.AccountLedger;
import com.bank.batch.entity.TransactionInput;
import com.bank.batch.exception.AccountServiceUnavailableException;
import com.bank.batch.writer.BatchWriteBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class TransactionProcessor
        implements ItemProcessor<TransactionInput, BatchWriteBundle> {

    private final AccountFeignClient client;
    private static final Logger log = LoggerFactory.getLogger(TransactionProcessor.class);

    public TransactionProcessor(AccountFeignClient client) {
        this.client = client;
    }

    @Override
    public BatchWriteBundle process(TransactionInput input) {

        System.out.println("Processing TXN -> " + input.getTxnId());

        ApiResponse<Account> acc;

        try {
            acc = client.getAccount(input.getAccountNo());
        } catch (Exception e) {
            log.error("Account service call failed for accountNumber={}", input.getAccountNo(), e);
            throw new AccountServiceUnavailableException(
                    "Account not found or account-service unavailable for accountNumber=" + input.getAccountNo(), e);
        }

        if (acc == null || !acc.isSuccess() || acc.getData() == null) {
            throw new RuntimeException("Account not found: " + input.getAccountNo());
        }

        Account account = acc.getData();

        if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            throw new RuntimeException("Inactive account");
        }

        BigDecimal balance = account.getBalance();

        AccountLedger ledger = new AccountLedger();
        ledger.setTxnId(input.getTxnId());
        ledger.setAccountNo(input.getAccountNo());

        if ("DEBIT".equalsIgnoreCase(input.getTxnType())) {

            if (balance.compareTo(input.getAmount()) < 0) {
                throw new RuntimeException("Insufficient balance");
            }

            ledger.setDebit(input.getAmount());
            ledger.setCredit(BigDecimal.ZERO);
            ledger.setBalanceAfter(balance.subtract(input.getAmount()));

        } else {

            ledger.setCredit(input.getAmount());
            ledger.setDebit(BigDecimal.ZERO);
            ledger.setBalanceAfter(balance.add(input.getAmount()));
        }

        BatchWriteBundle bundle = new BatchWriteBundle(input, ledger);
        bundle.setSourceFile(input.getSourceFile());

        return bundle;
    }
}
