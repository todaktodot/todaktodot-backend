package com.todaktodot.TDTD.domain.aireport.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReportResponseWrapDTO {
    @Schema(description = "AI 리포트 생성 가능 여부")
    private ReportCreateStatusResponseDTO createStatusResponseDTO;
    @Schema(description = "AI 리포트 상세")
    private ReportDetailResponseDTO detailResponseDTO;
}
