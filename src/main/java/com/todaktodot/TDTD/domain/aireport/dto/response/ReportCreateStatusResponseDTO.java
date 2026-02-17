package com.todaktodot.TDTD.domain.aireport.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 지난 한 주 AI 리포트 생성 가능 여부 및 최조 진입 여부
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class ReportCreateStatusResponseDTO {
    @Schema(description = "지난 한 주 AI 리포트 생성 가능 여부 - True: 리포트 생성 가능, False: 리포트 생성 불가", example = "true")
    private boolean isCreatable;

    @Schema(description = "현재 주 AI 리포트 최초 진입 여부 - True: 리포트 최초 진입, False: 최초 진입 아님", example = "false")
    private boolean isInitalize;

    @Schema(description = "AI 리포트ID")
    private long reportId;
}
