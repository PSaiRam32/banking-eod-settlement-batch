package com.bank.batch.listener;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import java.nio.file.*;

@Component
public class FileArchiveListener implements JobExecutionListener {

    @Value("${spring.batch.input.path}")
    private String inputPath;

    @Value("${spring.batch.archive.path}")
    private String archivePath;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        // Check input path for files before job starts. If none found, fail the job early
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(inputPath);

            if (resources == null || resources.length == 0) {
                String msg = "No input files found at path: " + inputPath + " - failing the job before processing.";
                System.out.println("========================================");
                System.out.println(msg);
                System.out.println("========================================");

                // Mark job as failed and add failure exception so the job won't proceed to steps
                jobExecution.setStatus(BatchStatus.FAILED);
                jobExecution.setExitStatus(ExitStatus.FAILED);
                jobExecution.addFailureException(new RuntimeException(msg));
            }
        } catch (Exception e) {
            String msg = "Error while checking input files at path: " + inputPath + " - failing the job.";
            System.out.println(msg);
            jobExecution.setStatus(BatchStatus.FAILED);
            jobExecution.setExitStatus(ExitStatus.FAILED);
            jobExecution.addFailureException(e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(msg, e));
        }
    }

    private boolean hasSuccessfulWrites(JobExecution jobExecution) {
        return jobExecution.getStepExecutions().stream()
                .anyMatch(step -> step.getWriteCount() > 0);
    }

    private long getTotalSkippedRecords(JobExecution jobExecution) {
        return jobExecution.getStepExecutions().stream()
                .mapToLong(step -> step.getSkipCount())
                .sum();
    }

    private boolean hasExcessiveSkips(JobExecution jobExecution) {
        long skippedCount = getTotalSkippedRecords(jobExecution);
        // Consider job failed for archival purposes when skipped records exceed 100
        return skippedCount > 100;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {

        // Check if more than 100 records were skipped (processing failure)
        if (hasExcessiveSkips(jobExecution)) {
            long skippedCount = getTotalSkippedRecords(jobExecution);
            System.out.println("========================================");
            System.out.println("BATCH PROCESSING FAILED");
            System.out.println("Skipped Records: " + skippedCount + " (exceeds 100)");
            System.out.println("Files remain in input folder: " + inputPath);
            System.out.println("No archival performed");
            System.out.println("========================================");
            return;
        }

        // Archive only if job completed AND meaningful records were written
        boolean isSuccessful = jobExecution.getStatus() == BatchStatus.COMPLETED
                && hasSuccessfulWrites(jobExecution);

        // Only archive files if job completed successfully
        if (!isSuccessful) {
            System.out.println("========================================");
            System.out.println("BATCH FAILED - Status: " + jobExecution.getStatus());
            System.out.println("Files remain in input folder: " + inputPath);
            System.out.println("No archival performed");
            System.out.println("========================================");
            return;
        }

        try {
            String cleanArchivePath = archivePath.replace("file:", "");
            Path archiveDir = Paths.get(cleanArchivePath);

            if (!Files.exists(archiveDir)) {
                Files.createDirectories(archiveDir);
            }

            Resource[] resources =
                    new PathMatchingResourcePatternResolver()
                            .getResources(inputPath);

            for (Resource r : resources) {

                Path source = r.getFile().toPath();
                Path target = archiveDir.resolve(r.getFilename());

                Files.move(
                        source,
                        target,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }
            System.out.println("========================================");
            System.out.println("Files archived successfully");
            System.out.println("Archived to: " + cleanArchivePath);
            System.out.println("========================================");
        } catch (Exception e) {
            throw new RuntimeException("File archival failed", e);
        }
    }
}
