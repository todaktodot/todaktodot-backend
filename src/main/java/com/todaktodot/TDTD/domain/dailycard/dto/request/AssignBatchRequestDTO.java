package com.todaktodot.TDTD.domain.dailycard.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "데일리카드 배정 배치 요청")
public class AssignBatchRequestDTO {

    @NotNull(message = "배정 시작일은 필수입니다")
    @Schema(description = "배정 시작일", example = "2026-01-18")
    private LocalDate startDate;

    @NotNull(message = "배정 종료일은 필수입니다")
    @Schema(description = "배정 종료일", example = "2026-01-24")
    private LocalDate endDate;

    @AssertTrue(message = "배정 기간은 시작일 기준 1~7일(같은 날 포함)이어야 합니다")
    public boolean isValidRange() {
        if (startDate == null || endDate == null) {
            return true;
        }
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        return daysBetween >= 0 && daysBetween <= 6;
    }
}
