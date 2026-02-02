package com.bank.batch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bank.batch.entity.AccountLedger;

public interface AccountLedgerRepository
        extends JpaRepository<AccountLedger, Long> {
}
