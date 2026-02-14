package com.bank.batch.listener;

import com.bank.batch.entity.FailedTransaction;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BatchMetricsListener extends JobExecutionListenerSupport {

    private final MeterRegistry meterRegistry;
    private final JavaMailSender mailSender;
    private final FailedTransactionHolder failedTransactionHolder;

    public BatchMetricsListener(MeterRegistry meterRegistry,
                                JavaMailSender mailSender,
                                FailedTransactionHolder failedTransactionHolder) {
        this.meterRegistry = meterRegistry;
        this.mailSender = mailSender;
        this.failedTransactionHolder = failedTransactionHolder;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {

        int totalRead = 0;
        int totalWrite = 0;
        int totalSkip = 0;

        for (StepExecution se : jobExecution.getStepExecutions()) {
            totalRead += se.getReadCount();
            totalWrite += se.getWriteCount();
            totalSkip += se.getSkipCount();
        }

        System.out.println("=================================");
        System.out.println("JOB NAME  : " + jobExecution.getJobInstance().getJobName());
        System.out.println("STATUS    : " + jobExecution.getStatus());
        System.out.println("READ      : " + totalRead);
        System.out.println("WRITTEN   : " + totalWrite);
        System.out.println("SKIPPED   : " + totalSkip);

        // Log batch failure status
        if (jobExecution.getStatus().isUnsuccessful()) {
            System.out.println("BATCH FAILED");
            System.out.println("Input files remain in: " + jobExecution.getExecutionContext());
            System.out.println("Please review the batch failure and retry.");
        }

        System.out.println("=================================");

        // ----------------- EMAIL -----------------

        StringBuilder mailBody = new StringBuilder();
        mailBody.append("Banking EOD Batch Job Completed\n\n");
        mailBody.append(String.format("Job Name : %s\n", jobExecution.getJobInstance().getJobName()));
        mailBody.append(String.format("Status   : %s\n\n", jobExecution.getStatus()));
        mailBody.append(String.format("Records Read    : %d\n", totalRead));
        mailBody.append(String.format("Records Written : %d\n", totalWrite));
        mailBody.append(String.format("Records Skipped : %d\n\n", totalSkip));

        // Add failed transaction details if any
        List<FailedTransaction> failures = failedTransactionHolder.getFailures();
        if (failures != null && !failures.isEmpty()) {
            mailBody.append("Failed Transactions:\n");
            for (FailedTransaction f : failures) {
                mailBody.append("File name: ").append(f.getFileName() == null ? "<unknown>" : f.getFileName()).append("\n");
                mailBody.append("Failed Transaction ID: ").append(f.getTxnId() == null ? "<unknown>" : f.getTxnId()).append("\n");
                mailBody.append("Failure reason: ").append(f.getReason() == null ? "<unknown>" : f.getReason()).append("\n\n");
            }
        }

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo("rmavayya1@gmail.com");
        mail.setSubject("EOD Settlement Batch Job Status" + (jobExecution.getStatus().isUnsuccessful() ? " - FAILED" : " - SUCCESS"));
        mail.setText(mailBody.toString());

        try {
            mailSender.send(mail);
            System.out.println("Email sent successfully");
        } catch (Exception e) {
            System.out.println("Email failed: " + e.getMessage());
        }

        // clear failures after sending summary
        failedTransactionHolder.clear();

    }
}
