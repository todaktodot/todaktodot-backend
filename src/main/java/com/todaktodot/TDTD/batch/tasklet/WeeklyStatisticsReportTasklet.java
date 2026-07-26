package com.todaktodot.TDTD.batch.tasklet;

import com.todaktodot.TDTD.admin.statistics.service.WeeklyStatisticsDiscordReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyStatisticsReportTasklet implements Tasklet {

    private final WeeklyStatisticsDiscordReportService weeklyStatisticsDiscordReportService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("=====주간 통계 Discord 리포트 배치 시작=====");
        weeklyStatisticsDiscordReportService.sendPreviousWeekReport();
        log.info("=====주간 통계 Discord 리포트 배치 완료=====");
        return RepeatStatus.FINISHED;
    }
}
