package com.bank.batch.listener;

import com.bank.batch.entity.FailedTransaction;
import com.bank.batch.entity.TransactionInput;
import com.bank.batch.writer.BatchWriteBundle;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionSkipListener implements SkipListener<TransactionInput, BatchWriteBundle> {

    private final FailedTransactionHolder holder;

    public TransactionSkipListener(FailedTransactionHolder holder) {
        this.holder = holder;
    }

    @Override
    public void onSkipInRead(Throwable t) {
        // not used
    }

    @Override
    public void onSkipInWrite(BatchWriteBundle item, Throwable t) {
        // If write fails, record txn id and reason if available
        if (item != null && item.getTransactionInput() != null) {
            TransactionInput tx = item.getTransactionInput();
            String file = item.getSourceFile();
            if (file == null) file = tx.getSourceFile();
            String txnId = tx.getTxnId();
            String reason = t == null ? "Unknown write failure" : t.getMessage();
            holder.add(new FailedTransaction(file, txnId, reason));
        }
    }

    @Override
    public void onSkipInProcess(TransactionInput item, Throwable t) {
        if (item != null) {
            String file = item.getSourceFile();
            String txnId = item.getTxnId();
            String reason = t == null ? "Unknown processing failure" : t.getMessage();
            holder.add(new FailedTransaction(file, txnId, reason));
        }
    }
}
