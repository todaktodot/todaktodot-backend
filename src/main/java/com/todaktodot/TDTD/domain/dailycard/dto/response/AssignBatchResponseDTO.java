package com.todaktodot.TDTD.domain.dailycard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
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

    @Builder.Default
    @Schema(description = "이번 실행 상세 결과")
    private List<AssignedDayDetail> assignedDayDetails = List.of();

    @Getter
    @Builder
    @AllArgsConstructor
    public static class AssignedDayDetail {

        @Schema(description = "커플 ID", example = "42")
        private Long coupleId;

        @Schema(description = "커플 이름", example = "민수 / 지수")
        private String coupleName;

        @Schema(description = "발급 일자", example = "2026-01-18")
        private LocalDate issuedDate;

        @Schema(description = "기존 배정으로 스킵 여부", example = "false")
        private boolean skipped;

        @Schema(description = "질문 모드 코드", example = "DESSERT")
        private String mode;

        @Schema(description = "질문 모드 표시명", example = "디저트")
        private String modeDisplayName;

        @Schema(description = "질문 주제 코드", example = "LOVE")
        private String subject;

        @Schema(description = "질문 주제 표시명", example = "연애관")
        private String subjectDisplayName;

        @Builder.Default
        @Schema(description = "이번 실행에서 배정된 카드 목록")
        private List<AssignedCardDetail> cards = List.of();
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class AssignedCardDetail {

        @Schema(description = "카드 ID", example = "1001")
        private Long cardId;

        @Schema(description = "카드 제목", example = "서로의 연애 습관 이야기 나누기")
        private String cardTitle;

        @Schema(description = "질문 유형 코드", example = "ROLEPLAY")
        private String type;

        @Schema(description = "질문 유형 표시명", example = "상황극")
        private String typeDisplayName;
    }
}
