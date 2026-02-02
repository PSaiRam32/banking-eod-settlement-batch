package com.bank.batch.writer;

import com.bank.batch.entity.AccountLedger;
import com.bank.batch.entity.TransactionInput;
import com.bank.batch.repository.AccountLedgerRepository;
import com.bank.batch.repository.TransactionInputRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DualTableItemWriter implements ItemWriter<BatchWriteBundle> {

    private final TransactionInputRepository transactionRepo;
    private final AccountLedgerRepository ledgerRepo;

    public DualTableItemWriter(TransactionInputRepository transactionRepo,
                               AccountLedgerRepository ledgerRepo) {
        this.transactionRepo = transactionRepo;
        this.ledgerRepo = ledgerRepo;
    }

    @Override
    public void write(Chunk<? extends BatchWriteBundle> items) {

        if (items == null || items.isEmpty()) {
            System.out.println("Writer received EMPTY chunk");
            return;
        }

        List<TransactionInput> transactions = new ArrayList<>();
        List<AccountLedger> ledgers = new ArrayList<>();

        for (BatchWriteBundle bundle : items) {

            if (bundle.getTransactionInput() != null) {
                transactions.add(bundle.getTransactionInput());
            }

            if (bundle.getAccountLedger() != null) {
                ledgers.add(bundle.getAccountLedger());
            }
        }

        System.out.println("➡ Writing Transactions: " + transactions.size());
        System.out.println("➡ Writing Ledgers: " + ledgers.size());

        transactionRepo.saveAll(transactions);
        ledgerRepo.saveAll(ledgers);
    }
}
