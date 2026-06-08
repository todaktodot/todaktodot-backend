package com.todaktodot.TDTD.admin.statistics.service;

import com.todaktodot.TDTD.admin.statistics.dto.StatisticsPageDTO;
import com.todaktodot.TDTD.admin.statistics.dto.WeeklyStatisticsDTO;
import com.todaktodot.TDTD.admin.statistics.report.WeeklyStatisticsReportFormatter;
import com.todaktodot.TDTD.global.alert.DiscordNotificationService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyStatisticsDiscordReportService {

    private final AdminStatisticsService adminStatisticsService;
    private final WeeklyStatisticsReportFormatter weeklyStatisticsReportFormatter;
    private final DiscordNotificationService discordNotificationService;

    public void sendPreviousWeekReport() {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.with(TemporalAdjusters.previous(DayOfWeek.TUESDAY));
        LocalDate startDate = endDate.minusDays(6);

        StatisticsPageDTO page = adminStatisticsService.getWeeklyStatistics(startDate, endDate);
        WeeklyStatisticsDTO statistic = page.getLatestWeek();
        if (statistic == null) {
            log.warn("주간 통계 리포트 대상 데이터가 없습니다. startDate={}, endDate={}", startDate, endDate);
            return;
        }

        discordNotificationService.sendStatisticsReport(
                weeklyStatisticsReportFormatter.buildTitle(statistic),
                weeklyStatisticsReportFormatter.buildDescription(statistic),
                weeklyStatisticsReportFormatter.buildFields(statistic)
        );
    }
}
