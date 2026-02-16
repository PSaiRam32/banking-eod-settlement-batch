package com.bank.batch.config;

import com.bank.batch.entity.*;
import com.bank.batch.listener.BatchMetricsListener;
import com.bank.batch.listener.FileArchiveListener;
import com.bank.batch.listener.TransactionSkipListener;
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
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
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

    /**
     * Thread pool for chunk-level parallel processing.
     * Adjust core/max/queue sizes to your environment and DB concurrency limits.
     */
    @Bean
    public TaskExecutor batchTaskExecutor() {
        ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
        te.setCorePoolSize(5); // Minimum number of threads to keep alive
        te.setMaxPoolSize(10); // Maximum number of threads in the pool
        te.setQueueCapacity(50); // Queue size for tasks before new threads are created
        te.setThreadNamePrefix("batch-worker-");
        te.initialize(); // Initialize the thread pool
        return te;
    }

    /**
     * Wrap the stateful MultiResourceItemReader in a SynchronizedItemStreamReader
     * so concurrent chunk threads can safely call read/open/close.
     */

    @Bean
    public SynchronizedItemStreamReader<TransactionInput> synchronizedReader(MultiResourceItemReader<TransactionInput> delegate) {
        SynchronizedItemStreamReader<TransactionInput> sync = new SynchronizedItemStreamReader<>();
        sync.setDelegate(delegate);
        return sync;
    }

    /**
     * Multi-threaded chunk step. Uses the synchronized reader and a TaskExecutor.
     * throttleLimit should be <= maxPoolSize to control concurrent threads.
     */
    @Bean
    public Step MasterDataStep(JobRepository repo,
                               PlatformTransactionManager tx,
                               SynchronizedItemStreamReader<TransactionInput> reader,
                               ItemProcessor<TransactionInput, BatchWriteBundle> processor,
                               DualTableItemWriter writer,
                               TransactionSkipListener skipListener,
                               TaskExecutor batchTaskExecutor) {

        return new StepBuilder("MasterDataStep", repo)
                .<TransactionInput, BatchWriteBundle>chunk(50, tx)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skip(RuntimeException.class)
                .skipLimit(1000)
                .listener(skipListener)
                .taskExecutor(batchTaskExecutor)
                .build();
    }
}
