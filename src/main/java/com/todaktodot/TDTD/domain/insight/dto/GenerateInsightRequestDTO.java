package com.todaktodot.TDTD.domain.insight.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 인사이트 생성시 요청 DTO
 */
@Getter
@NoArgsConstructor
@Schema(description = "리포트 AI 인사이트 생성 요청")
public class GenerateInsightRequestDTO {

    @NotNull(message = "커플 ID는 필수입니다")
    @Schema(description = "커플 ID")
    private Long coupleId;

    @NotNull(message = "생성 종료 일자는 필수입니다.")
    @Schema(description = "인사이트 생성 종료일자", example = "2026-01-19")
    private LocalDate endDt;
}
