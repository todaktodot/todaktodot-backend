package com.todaktodot.TDTD.admin.statistics.service;

import com.todaktodot.TDTD.admin.statistics.dto.StatisticsPageDTO;
import java.time.LocalDate;

public interface AdminStatisticsService {
    StatisticsPageDTO getWeeklyStatistics(LocalDate startDate, LocalDate endDate);

    byte[] exportWeeklyStatisticsCsv(LocalDate startDate, LocalDate endDate);
}
