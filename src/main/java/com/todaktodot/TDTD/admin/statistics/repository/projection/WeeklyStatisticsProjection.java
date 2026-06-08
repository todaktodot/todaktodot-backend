package com.todaktodot.TDTD.admin.statistics.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface WeeklyStatisticsProjection {
    LocalDate getPeriodStartDate();

    LocalDate getPeriodEndDate();

    Long getTotalUserCount();

    Long getTotalCoupleCount();

    Long getDailyCardCount();

    Long getAnsweredUserCount();

    BigDecimal getPersonalAnswerRate();

    Long getHistoryCardCount();

    Long getAiFeedbackCount();

    BigDecimal getCoupleBothAnswerRate();
}
