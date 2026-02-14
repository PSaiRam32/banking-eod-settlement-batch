package com.bank.batch.writer;

import com.bank.batch.entity.*;


public class BatchWriteBundle {

    private TransactionInput transactionInput;
    private AccountLedger accountLedger;
    // transient field to carry source file name through the bundle
    private String sourceFile;

    public BatchWriteBundle(TransactionInput t, AccountLedger l) {
        this.transactionInput = t;
        this.accountLedger = l;
        if (t != null) this.sourceFile = t.getSourceFile();
    }

    public TransactionInput getTransactionInput() {
        return transactionInput;
    }

    public AccountLedger getAccountLedger() {
        return accountLedger;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }
}
