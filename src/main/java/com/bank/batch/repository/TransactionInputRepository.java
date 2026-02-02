package com.bank.batch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bank.batch.entity.TransactionInput;

public interface TransactionInputRepository
        extends JpaRepository<TransactionInput, Long> {
}
