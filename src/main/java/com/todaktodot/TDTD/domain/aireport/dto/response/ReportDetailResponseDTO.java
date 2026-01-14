package com.todaktodot.TDTD.domain.aireport.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 리포트 상세
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class ReportDetailResponseDTO {
    @Schema(description = "AI리포트ID")
    private Long reportId;
    @Schema(description = "주간시작일자")
    private LocalDateTime startDt;
    @Schema(description = "주간종료일자")
    private LocalDateTime endDt;
    @Schema(description = "전체 싱크로율", example = "78")
    private String totalSyncRate;
    @Schema(description = "경제관 싱크로율", example = "45")
    private String economySyncRate;
    @Schema(description = "생활관 싱크로율", example = "90")
    private String lifeSyncRate;
    @Schema(description = "연애관 싱크로율", example = "80")
    private String loveSyncRate;
    @Schema(description = "대화 참여율", example = "83")
    private String dailycardAnswerRate;
    @Schema(description = "대화 누적 자산", example = "127")
    private String totalDailycardAnswerCnt;
    @Schema(description = "비슷했던 주제 목록")
    private List<SimpleDailycardInfoDTO> similarSubjectList;
    @Schema(description = "대화가 더 필요한 주제 목록")
    private List<SimpleDailycardInfoDTO> diffrentSubjectList;

    static private class SimpleDailycardInfoDTO {
        @Schema(description = "응답ID-1")
        private Long answerId1;
        @Schema(description = "응답ID-2")
        private Long answerId2;
        @Schema(description = "데일리카드ID")
        private Long cardId;
        @Schema(description = "응답 날짜")
        private LocalDateTime answerDt;
        @Schema(description = "데일리카드 모드", example = "위스키모드")
        private String mode;
        @Schema(description = "데일리카드 주제", example = "경제관")
        private String subject;
    }

}
