package com.bank.batch.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JobLauncherController {

    private final JobLauncher jobLauncher;
    private final Job eodJob;

    public JobLauncherController(
            JobLauncher jobLauncher,
            @Qualifier("eodJob") Job eodJob) {
        this.jobLauncher = jobLauncher;
        this.eodJob = eodJob;
    }

    @PostMapping("/runbankingEodSettlementJob")
    public String runJob() throws Exception {

        JobParameters params = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncher.run(eodJob, params);

        return "EOD Job Started. Execution Id = " + execution.getId();
    }
}
