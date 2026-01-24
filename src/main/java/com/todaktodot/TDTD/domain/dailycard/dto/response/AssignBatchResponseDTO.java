package com.todaktodot.TDTD.domain.dailycard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "데일리카드 배정 배치 응답")
public class AssignBatchResponseDTO {

    @Schema(description = "배정 시작 일자", example = "2026-01-18")
    private LocalDate startDate;

    @Schema(description = "배정 종료 일자", example = "2026-01-24")
    private LocalDate endDate;

    @Schema(description = "배정 기간(일수)", example = "7")
    private int days;

    @Schema(description = "배정 대상 커플 수", example = "42")
    private int coupleCount;

    @Schema(description = "배정된 카드 수", example = "84")
    private int assignedCount;

    @Schema(description = "이미 배정되어 건너뛴 날짜 수", example = "3")
    private int skippedDateCount;
}
