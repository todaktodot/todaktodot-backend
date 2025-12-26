package com.todaktodot.TDTD.domain.dailycard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "데일리카드 답변 제출 응답")
public class SubmitAnswerResponseDTO {

    @Schema(description = "커플 카드 ID (COUPLE_DAILY_CARD 테이블 PK)", example = "1")
    private Long coupleCardId;

    @Schema(description = "데일리카드 ID (DAILY_CARD 테이블 참조)", example = "1")
    private Long cardId;

    @Schema(description = "답변한 사용자 ID", example = "42")
    private Long userId;

    @Schema(description = "저장된 답변 수", example = "2")
    private int savedCount;

    @Schema(description = "저장된 답변 목록")
    private List<SavedAnswer> savedAnswers;

    @Schema(description = "저장 일시")
    private LocalDateTime savedAt;

    @Getter
    @Builder
    @Schema(description = "저장된 개별 답변 정보")
    public static class SavedAnswer {

        @Schema(description = "답변 ID", example = "1")
        private Long answerId;

        @Schema(description = "질문 번호", example = "1")
        private Integer questionNo;

        @Schema(description = "답변 내용", example = "2")
        private String answerContent;
    }

    public static SubmitAnswerResponseDTO of(Long coupleCardId, Long cardId, Long userId, List<SavedAnswer> savedAnswers) {
        return SubmitAnswerResponseDTO.builder()
                .coupleCardId(coupleCardId)
                .cardId(cardId)
                .userId(userId)
                .savedCount(savedAnswers.size())
                .savedAnswers(savedAnswers)
                .savedAt(LocalDateTime.now())
                .build();
    }
}
