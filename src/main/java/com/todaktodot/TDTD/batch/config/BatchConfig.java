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
    public Job dailyCardAssignAlarmJob(JobRepository jobRepository, Step dailyCardAssignAlarmStep) {
        return new JobBuilder("dailyCardAssignAlarmJob", jobRepository)
                .start(dailyCardAssignAlarmStep)
                .build();
    }

    @Bean
    public Step dailyCardAssignAlarmStep(JobRepository jopRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("dailyCardAssignAlarmStep", jopRepository)
                .tasklet(dailyCardAssignTasklet, transactionManager)
                .build();
    }

    //AI 리포트 도착 알림
    @Bean
    public Job aiReportAssignAlarmJob(JobRepository jobRepository, Step aiReportAssignAlarmStep) {
        return new JobBuilder("aiReportAssignAlarmJob", jobRepository)
                .start(aiReportAssignAlarmStep)
                .build();
    }

    @Bean
    public Step aiReportAssignAlarmStep(JobRepository jopRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("aiReportAssignAlarmStep", jopRepository)
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
