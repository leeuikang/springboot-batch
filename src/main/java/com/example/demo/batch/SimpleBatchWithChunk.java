package com.example.demo.batch;

import com.example.demo.executor.BatchExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class SimpleBatchWithChunk {
    public static final int POOL_SIZE = 10;

    @Bean
    public Job simpleJobWithChunk(JobRepository jobRepository, Step step) {
        return new JobBuilder("simpleJobWithChunk", jobRepository)
                .start(step)
                .build();
    }

    @Bean
    @JobScope
    public Step simpleStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        BatchExecutor batchExecutor = new BatchExecutor();
        return new StepBuilder("simpleStep", jobRepository)
                .<String, String>chunk(5, transactionManager)
                .reader(new ListItemReader<>(Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")))
                .processor(item -> "item = " + item)
                .writer(items -> {
                    Thread.sleep(1000);
                    log.info("item = {}", items);
                })
                .taskExecutor(batchExecutor.build(POOL_SIZE))
                .throttleLimit(POOL_SIZE - 1)
                .build();
    }

}
