package com.todaktodot.TDTD.admin.statistics.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StatisticsPageDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private List<WeeklyStatisticsDTO> weeklyStatistics;
    private WeeklyStatisticsDTO latestWeek;
    private String chartDataJson;
}
