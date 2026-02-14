package com.bank.batch.listener;

import com.bank.batch.entity.FailedTransaction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class FailedTransactionHolder {

    private final List<FailedTransaction> failures = Collections.synchronizedList(new ArrayList<>());

    public void add(FailedTransaction ft) {
        failures.add(ft);
    }

    public List<FailedTransaction> getFailures() {
        return new ArrayList<>(failures);
    }

    public void clear() {
        failures.clear();
    }
}

