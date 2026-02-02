package com.bank.batch.writer;

import com.bank.batch.entity.*;


public class BatchWriteBundle {

    private TransactionInput transactionInput;
    private AccountLedger accountLedger;

    public BatchWriteBundle(TransactionInput t, AccountLedger l) {
        this.transactionInput = t;
        this.accountLedger = l;
    }

    public TransactionInput getTransactionInput() {
        return transactionInput;
    }

    public AccountLedger getAccountLedger() {
        return accountLedger;
    }
}
