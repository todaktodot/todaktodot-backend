package com.todaktodot.TDTD.admin.dailycard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CardStatisticsDTO {
    private long totalCount;
    private long activeCount;
    private long inactiveCount;

    public static CardStatisticsDTO from(Object[] row) {
        return new CardStatisticsDTO(
                ((Number) row[0]).longValue(),
                row[1] != null ? ((Number) row[1]).longValue() : 0L,
                row[2] != null ? ((Number) row[2]).longValue() : 0L
        );
    }
}
