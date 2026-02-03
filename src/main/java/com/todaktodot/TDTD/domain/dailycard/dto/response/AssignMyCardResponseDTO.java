package com.todaktodot.TDTD.domain.dailycard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "내 커플 데일리카드 배정 응답")
public class AssignMyCardResponseDTO {

    @Schema(description = "커플 ID", example = "1")
    private Long coupleId;

    @Schema(description = "배정 시작 일자", example = "2026-02-03")
    private LocalDate startDate;

    @Schema(description = "배정 종료 일자", example = "2026-02-09")
    private LocalDate endDate;

    @Schema(description = "새로 배정된 카드 수", example = "14")
    private int assignedCount;

    @Schema(description = "이미 배정되어 스킵된 날짜 수", example = "0")
    private int skippedDateCount;
}
