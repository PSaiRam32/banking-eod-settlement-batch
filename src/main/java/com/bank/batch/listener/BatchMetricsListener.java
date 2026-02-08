package com.bank.batch.listener;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class BatchMetricsListener extends JobExecutionListenerSupport {

    private final MeterRegistry meterRegistry;
    private final JavaMailSender mailSender;

    public BatchMetricsListener(MeterRegistry meterRegistry,
                                JavaMailSender mailSender) {
        this.meterRegistry = meterRegistry;
        this.mailSender = mailSender;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {

        long totalRead = 0;
        long totalWrite = 0;
        long totalSkip = 0;

        for (StepExecution step : jobExecution.getStepExecutions()) {

            // ----------------- MICROMETER METRICS -----------------

            meterRegistry.counter(
                    "batch.records.read",
                    "step", step.getStepName()
            ).increment(step.getReadCount());

            meterRegistry.counter(
                    "batch.records.written",
                    "step", step.getStepName()
            ).increment(step.getWriteCount());

            meterRegistry.counter(
                    "batch.records.skipped",
                    "step", step.getStepName()
            ).increment(step.getSkipCount());

            // ----------------- TOTAL COUNTS -----------------

            totalRead += step.getReadCount();
            totalWrite += step.getWriteCount();
            totalSkip += step.getSkipCount();
        }

        // ----------------- CONSOLE OUTPUT -----------------

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

        String mailBody = """
                Banking EOD Batch Job Completed

                Job Name : %s
                Status   : %s

                Records Read    : %d
                Records Written : %d
                Records Skipped : %d
                """.formatted(
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getStatus(),
                totalRead,
                totalWrite,
                totalSkip
        );

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo("rmavayya1@gmail.com");
        mail.setSubject("EOD Settlement Batch Job Status"+(jobExecution.getStatus().isUnsuccessful() ? " - FAILED" : " - SUCCESS"));
        mail.setText(mailBody);

        try {
            mailSender.send(mail);
            System.out.println("Email sent successfully");
        } catch (Exception e) {
            System.out.println("Email failed: " + e.getMessage());
        }

    }
}
