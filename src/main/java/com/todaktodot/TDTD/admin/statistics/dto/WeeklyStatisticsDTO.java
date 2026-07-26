package com.todaktodot.TDTD.admin.statistics.dto;

import com.todaktodot.TDTD.admin.statistics.repository.projection.WeeklyStatisticsProjection;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WeeklyStatisticsDTO {

    private static final DateTimeFormatter LABEL_FORMATTER = DateTimeFormatter.ofPattern("MM.dd");

    private LocalDate periodStartDate;
    private LocalDate periodEndDate;
    private String periodLabel;
    private Long totalUserCount;
    private Long totalCoupleCount;
    private Long dailyCardCount;
    private Long answeredUserCount;
    private BigDecimal personalAnswerRate;
    private Long historyCardCount;
    private Long aiFeedbackCount;
    private BigDecimal coupleBothAnswerRate;

    public static WeeklyStatisticsDTO from(WeeklyStatisticsProjection projection) {
        LocalDate startDate = projection.getPeriodStartDate();
        LocalDate endDate = projection.getPeriodEndDate();

        return WeeklyStatisticsDTO.builder()
                .periodStartDate(startDate)
                .periodEndDate(endDate)
                .periodLabel(startDate.format(LABEL_FORMATTER) + " ~ " + endDate.format(LABEL_FORMATTER))
                .totalUserCount(defaultLong(projection.getTotalUserCount()))
                .totalCoupleCount(defaultLong(projection.getTotalCoupleCount()))
                .dailyCardCount(defaultLong(projection.getDailyCardCount()))
                .answeredUserCount(defaultLong(projection.getAnsweredUserCount()))
                .personalAnswerRate(defaultDecimal(projection.getPersonalAnswerRate()))
                .historyCardCount(defaultLong(projection.getHistoryCardCount()))
                .aiFeedbackCount(defaultLong(projection.getAiFeedbackCount()))
                .coupleBothAnswerRate(defaultDecimal(projection.getCoupleBothAnswerRate()))
                .build();
    }

    private static Long defaultLong(Long value) {
        return value != null ? value : 0L;
    }

    private static BigDecimal defaultDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
