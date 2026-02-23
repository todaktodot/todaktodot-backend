package com.todaktodot.TDTD.domain.dailycard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@Schema(description = "주간 데일리카드 조회 응답")
public class WeeklyCardResponseDTO {

    @Schema(description = "조회 시작일")
    private LocalDate startDate;

    @Schema(description = "조회 종료일")
    private LocalDate endDate;

    @Schema(description = "데일리카드 목록")
    private List<DailyCardItem> dailyCards;

    @Getter
    @Builder
    @Schema(description = "데일리카드 항목")
    public static class DailyCardItem {
        @Schema(description = "커플 카드 ID")
        private Long coupleCardId;
        @Schema(description = "카드 ID")
        private Long cardId;
        @Schema(description = "배정 날짜")
        private LocalDate issuedDate;
        @Schema(description = "카드 제목")
        private String cardTitle;
        @Schema(description = "모드")
        private String mode;
        @Schema(description = "주제")
        private String subject;
        @Schema(description = "유형")
        private String type;
        @Schema(description = "상황")
        private String situation;
        @Schema(description = "질문 목록")
        private List<QuestionItem> questions;
    }

    @Getter
    @Builder
    @Schema(description = "질문 항목")
    public static class QuestionItem {
        @Schema(description = "질문 번호")
        private Integer questionNo;
        @Schema(description = "질문 유형 (MULTIPLE_CHOICE / SUBJECTIVE)")
        private String questionType;
        @Schema(description = "질문 내용")
        private String questionCnts;
        @Schema(description = "답변 필수 여부 (Y/N)")
        private String answerRequired;
        @Schema(description = "선택지 목록 (주관식이면 빈 배열)")
        private List<OptionItem> options;
    }

    @Getter
    @Builder
    @Schema(description = "선택지 항목")
    public static class OptionItem {
        @Schema(description = "선택지 번호")
        private Integer optionNo;
        @Schema(description = "선택지 내용")
        private String optionCnts;
    }
}
