package com.example.demo.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.concurrent.TimeUnit;

/**
 * @EnableBatchProcessing이 있는경우 일정 버전이상에선 실행이 안된다
 * https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide#spring-batch-changes //참고 url
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
public class SimpleBatch {

    private int poolSize = 4;

    @Bean
    public Job simpleJob(JobRepository jobRepository, Step step){
        return new JobBuilder("simpleJob", jobRepository)
                .start(step)
                .build();
    }

    @Bean
    public Step simpleStep(JobRepository jobRepository, PlatformTransactionManager transactionManager){
        return new StepBuilder("simpleStep", jobRepository)
                .tasklet(simpleTasklet(), transactionManager)
                .taskExecutor(executor())
                .build();
    }

    @Bean
    public Tasklet simpleTasklet(){
        return (stepContribution, chunkContext) -> {
            for(int i = 0; i < 10; i++){
                System.out.println(i);
                Thread.sleep(1);
            }
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public TaskExecutor executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor(); // (2)
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);
        executor.setThreadNamePrefix("multi-thread-");
        executor.setWaitForTasksToCompleteOnShutdown(Boolean.TRUE);
        executor.initialize();
        return executor;
    }

}
