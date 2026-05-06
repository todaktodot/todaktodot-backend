package com.todaktodot.TDTD.domain.feedback.dto.response;

import com.todaktodot.TDTD.domain.feedback.repository.entity.DailyCardFeedbackEntity;
import com.todaktodot.TDTD.domain.feedback.repository.entity.FeedbackGenerationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "AI 피드백 상태/결과 응답")
public class FeedbackResponseDTO {

    @Schema(
            description = FeedbackGenerationStatus.SWAGGER_DESCRIPTION,
            allowableValues = {"NOT_STARTED", "GENERATING", "COMPLETED", "FAILED"},
            example = "COMPLETED"
    )
    private final String feedbackStatus;

    @Schema(description = "AI 피드백 본문. feedbackStatus=COMPLETED일 때 포함되고, NOT_STARTED/GENERATING/FAILED 상태에서는 null입니다.")
    private final FeedbackItem feedback;

    public FeedbackResponseDTO(String feedbackStatus, FeedbackItem feedback) {
        this.feedbackStatus = feedbackStatus;
        this.feedback = feedback;
    }

    public static FeedbackResponseDTO completed(DailyCardFeedbackEntity feedback) {
        return new FeedbackResponseDTO(
                FeedbackGenerationStatus.COMPLETED.name(),
                FeedbackItem.from(feedback)
        );
    }

    public static FeedbackResponseDTO notStarted() {
        return new FeedbackResponseDTO(FeedbackGenerationStatus.NOT_STARTED.name(), null);
    }

    public static FeedbackResponseDTO generating() {
        return new FeedbackResponseDTO(FeedbackGenerationStatus.GENERATING.name(), null);
    }

    public static FeedbackResponseDTO failed() {
        return new FeedbackResponseDTO(FeedbackGenerationStatus.FAILED.name(), null);
    }

    public static FeedbackResponseDTO from(DailyCardFeedbackEntity feedback) {
        return completed(feedback);
    }

    @Getter
    @Schema(description = "AI 피드백 본문")
    public static class FeedbackItem {

        @Schema(description = "피드백 ID", example = "10")
        private final Long feedbackId;

        @Schema(description = "요약")
        private final String summary;

        @Schema(description = "공통점")
        private final String matchPoints;

        @Schema(description = "차이점")
        private final String differences;

        @Schema(description = "대화 시작 질문")
        private final String conversationStarter;

        public FeedbackItem(Long feedbackId, String summary, String matchPoints,
                            String differences, String conversationStarter) {
            this.feedbackId = feedbackId;
            this.summary = summary;
            this.matchPoints = matchPoints;
            this.differences = differences;
            this.conversationStarter = conversationStarter;
        }

        public static FeedbackItem from(DailyCardFeedbackEntity feedback) {
            return new FeedbackItem(
                    feedback.getFeedbackId(),
                    feedback.getSummary(),
                    feedback.getMatchPoints(),
                    feedback.getDifferences(),
                    feedback.getConversationStarter()
            );
        }
    }
}
