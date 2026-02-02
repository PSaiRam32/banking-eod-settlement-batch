package com.bank.batch.config;


import com.bank.batch.entity.*;
import com.bank.batch.listener.BatchMetricsListener;
import com.bank.batch.listener.FileArchiveListener;
import com.bank.batch.processor.TransactionProcessor;
import com.bank.batch.writer.BatchWriteBundle;
import com.bank.batch.writer.DualTableItemWriter;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.*;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.support.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Bean
    public Job eodJob(JobRepository repo,
                      Step MasterDataStep,
                      Step emailStep,
                      FileArchiveListener archiveListener,
                      BatchMetricsListener metricsListener) {

        return new JobBuilder("bankingEodSettlementJob", repo)
                .incrementer(new RunIdIncrementer())
                .listener(archiveListener)
                .listener(metricsListener)
                .start(MasterDataStep)
                .build();
    }


    @Bean
    public Step MasterDataStep(JobRepository repo,
                     PlatformTransactionManager tx,
                                   MultiResourceItemReader<TransactionInput> reader,
                               ItemProcessor<TransactionInput, BatchWriteBundle> processor,
                               DualTableItemWriter writer) {

        return new StepBuilder("MasterDataStep", repo)
                .<TransactionInput, BatchWriteBundle>chunk(50, tx)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skip(RuntimeException.class)
                .skipLimit(1000)
                .build();
    }
}
