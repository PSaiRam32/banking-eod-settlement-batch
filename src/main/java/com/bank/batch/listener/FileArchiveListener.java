package com.bank.batch.listener;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.BatchStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;

//@Component
//public class FileArchiveListener implements JobExecutionListener {
//
//    @Value("${spring.batch.input.path}")
//    private String inputPath;
//
//    @Value("${spring.batch.archive.path}")
//    private String archivePath;
//
//
//    @Override
//    public void beforeJob(JobExecution jobExecution) {
//        // No action required
//    }
//
//    @Override
//    public void afterJob(JobExecution jobExecution) {
//
//        if (jobExecution.getStatus() != BatchStatus.COMPLETED) {
//            return;   // archive only if job succeeded
//        }
//
//        try {
//            Files.createDirectories(Paths.get(archivePath));
//            DirectoryStream<Path> files =
//                    Files.newDirectoryStream(Paths.get(inputPath), "*.csv");
//
//            for (Path file : files) {
//                Path target =
//                        Paths.get(archivePath, file.getFileName().toString());
//                Files.move(
//                        file,
//                        target,
//                        StandardCopyOption.REPLACE_EXISTING
//                );
//            }
//        } catch (IOException ex) {
//            throw new RuntimeException("File archival failed", ex);
//        }
//    }
//}

@Component
public class FileArchiveListener implements JobExecutionListener {

    @Value("${spring.batch.input.path}")
    private String inputPath;

    @Value("${spring.batch.archive.path}")
    private String archivePath;

    @Override
    public void afterJob(JobExecution jobExecution) {

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

            System.out.println("Files archived successfully");

        } catch (Exception e) {
            throw new RuntimeException("File archival failed", e);
        }
    }
}

