package com.bank.batch.reader;

import com.bank.batch.entity.TransactionInput;
import org.springframework.batch.item.file.*;
import org.springframework.batch.item.file.mapping.*;
import org.springframework.batch.item.file.transform.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Configuration
public class MultiFileReader {

    @Value("${spring.batch.input.path}")
    private String inputPath;

    // ---------------------------
    // MULTI RESOURCE READER (STEP WILL USE THIS)
    // ---------------------------
    @Bean
    public MultiResourceItemReader<TransactionInput> reader() throws Exception {

        MultiResourceItemReader<TransactionInput> reader =
                new MultiResourceItemReader<>();

        Resource[] resources =
                new PathMatchingResourcePatternResolver()
                        .getResources(inputPath);

        System.out.println("FILES FOUND = " + resources.length);
        for (Resource r : resources) {
            System.out.println("FILE => " + r.getFilename());
        }

        reader.setResources(resources);
        reader.setDelegate(transactionReader());
        reader.setStrict(false);

        return reader;
    }

    @Bean
    public FlatFileItemReader<TransactionInput> transactionReader() {

        FileAwareFlatFileItemReader reader = new FileAwareFlatFileItemReader();

        reader.setLinesToSkip(1);

        DelimitedLineTokenizer tokenizer =
                new DelimitedLineTokenizer();
        tokenizer.setDelimiter(",");
        tokenizer.setNames(
                "txnId",
                "accountNo",
                "txnType",
                "amount",
                "txnDate"
        );

        BeanWrapperFieldSetMapper<TransactionInput> delegateMapper =
                new BeanWrapperFieldSetMapper<>();
        delegateMapper.setTargetType(TransactionInput.class);

        DefaultConversionService conversionService =
                new DefaultConversionService();

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd");

        conversionService.addConverter(String.class, LocalDate.class,
                source -> LocalDate.parse(source, formatter));

        delegateMapper.setConversionService(conversionService);

        DefaultLineMapper<TransactionInput> lineMapper =
                new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);

        // Capture the reader instance in the mapper so we can read current file reliably
        lineMapper.setFieldSetMapper(fieldSet -> {
            TransactionInput t = delegateMapper.mapFieldSet(fieldSet);
            String file = reader.getCurrentFile();
            if (t != null) {
                t.setSourceFile(file);
            }
            return t;
        });

        reader.setLineMapper(lineMapper);

        return reader;
    }

    // Small subclass of FlatFileItemReader that captures the resource file name
    private static class FileAwareFlatFileItemReader extends FlatFileItemReader<TransactionInput> {

        private volatile String currentFile;

        @Override
        public void setResource(Resource resource) {
            super.setResource(resource);
            if (resource != null && resource.getFilename() != null) {
                this.currentFile = resource.getFilename();
            } else {
                this.currentFile = null;
            }
        }

        public String getCurrentFile() {
            return currentFile;
        }

        @Override
        public TransactionInput read() throws Exception {
            TransactionInput item = super.read();
            if (item != null && item.getSourceFile() == null) {
                item.setSourceFile(this.currentFile);
            }
            return item;
        }

        @Override
        public void close() {
            super.close();
            this.currentFile = null;
        }
    }

}
