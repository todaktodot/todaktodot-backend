package com.todaktodot.TDTD.batch.config;

import com.todaktodot.TDTD.batch.tasklet.AiReportAssignTasklet;
import com.todaktodot.TDTD.batch.tasklet.AiReportCreateTasklet;
import com.todaktodot.TDTD.batch.tasklet.DailyCardAssignTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
//@EnableBatchProcessing
public class BatchConfig {
    private final DailyCardAssignTasklet dailyCardAssignTasklet;
    private final AiReportAssignTasklet aiReportAssignTasklet;
    private final AiReportCreateTasklet aiReportCreateTasklet;

    public BatchConfig(DailyCardAssignTasklet dailyCardAssignTasklet, AiReportAssignTasklet aiReportAssignTasklet, AiReportCreateTasklet aiReportCreateTasklet) {
        this.dailyCardAssignTasklet = dailyCardAssignTasklet;
        this.aiReportAssignTasklet = aiReportAssignTasklet;
        this.aiReportCreateTasklet = aiReportCreateTasklet;
    }

    //데일리카드 도착 알림
    @Bean
    public Job dailyCardAssignJob(JobRepository jobRepository, Step dailyCardAssignStep) {
        return new JobBuilder("dailyCardAssignJob", jobRepository)
                .start(dailyCardAssignStep)
                .build();
    }

    @Bean
    public Step dailyCardAssignStep(JobRepository jopRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("dailyCardAssignStep", jopRepository)
                .tasklet(dailyCardAssignTasklet, transactionManager)
                .build();
    }

    //AI 리포트 도착 알림
    @Bean
    public Job aiReportAssignJob(JobRepository jobRepository, Step aiReportAssignStep) {
        return new JobBuilder("aiReportAssignJob", jobRepository)
                .start(aiReportAssignStep)
                .build();
    }

    @Bean
    public Step aiReportAssignStep(JobRepository jopRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("aiReportAssignStep", jopRepository)
                .tasklet(aiReportAssignTasklet, transactionManager)
                .build();
    }

    //AI 리포트 생성
    @Bean
    public Job aiReportCreateJob(JobRepository jobRepository, Step aiReportCreateStep) {
        return new JobBuilder("aiReportCreateJob", jobRepository)
                .start(aiReportCreateStep)
                .build();
    }

    @Bean
    public Step aiReportCreateStep(JobRepository jopRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("aiReportCreateStep", jopRepository)
                .tasklet(aiReportCreateTasklet, transactionManager)
                .build();
    }
}
