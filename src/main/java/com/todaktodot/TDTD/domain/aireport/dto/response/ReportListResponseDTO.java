package com.todaktodot.TDTD.domain.aireport.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportListResponseDTO {
    @Schema(description = "연 정보", example = "2025")
    private String yearMonth;
    @Schema(description = "월 정보", example = "9")
    private String month;
    @Schema(description = "주차 정보", example = "1")
    private String week;
    @Schema(description = "리포트ID")
    private Long reportId;
}
