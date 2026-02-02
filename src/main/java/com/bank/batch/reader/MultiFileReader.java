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

        FlatFileItemReader<TransactionInput> reader =
                new FlatFileItemReader<>();

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

        BeanWrapperFieldSetMapper<TransactionInput> mapper =
                new BeanWrapperFieldSetMapper<>();
        mapper.setTargetType(TransactionInput.class);

        DefaultConversionService conversionService =
                new DefaultConversionService();

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd");

        conversionService.addConverter(String.class, LocalDate.class,
                source -> LocalDate.parse(source, formatter));

        mapper.setConversionService(conversionService);

        DefaultLineMapper<TransactionInput> lineMapper =
                new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(mapper);

        reader.setLineMapper(lineMapper);

        return reader;
    }

}
