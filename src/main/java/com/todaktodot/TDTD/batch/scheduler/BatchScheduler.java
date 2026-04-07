package com.todaktodot.TDTD.batch.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job dailyCardAssignAlarmJob;
    private final Job aiReportAssignAlarmJob;
    private final Job aiReportCreateJob;

    // 매일 오전 8시에 한 번만 실행됨
    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Seoul")
    public void runJobs() throws Exception{
        // 1. 매일 실행되는 잡 먼저 무조건 실행
        runJob(dailyCardAssignAlarmJob);

        // 2. 오늘이 월요일인지 체크해서 맞다면 이어서 실행
        if (LocalDate.now().getDayOfWeek() == DayOfWeek.MONDAY) {
            runJob(aiReportAssignAlarmJob);
        }
    }

    // 매주 월요일 오전 4시 반에 한 번만 실행됨
    @Scheduled(cron = "0 30 4 * * MON", zone = "Asia/Seoul")
    public void runReportCreateJobs() throws Exception{
        runJob(aiReportCreateJob);
    }

    private void runJob(Job job){
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(job, jobParameters);
        } catch (Exception e) {
            System.err.println(job.getName() + " 실행 중 오류 발생: " + e.getMessage());
        }
    }
}
