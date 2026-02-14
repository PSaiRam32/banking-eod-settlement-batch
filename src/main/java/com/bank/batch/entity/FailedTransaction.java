package com.bank.batch.entity;

public class FailedTransaction {
    private final String fileName;
    private final String txnId;
    private final String reason;

    public FailedTransaction(String fileName, String txnId, String reason) {
        this.fileName = fileName;
        this.txnId = txnId;
        this.reason = reason;
    }

    public String getFileName() {
        return fileName;
    }

    public String getTxnId() {
        return txnId;
    }

    public String getReason() {
        return reason;
    }
}

