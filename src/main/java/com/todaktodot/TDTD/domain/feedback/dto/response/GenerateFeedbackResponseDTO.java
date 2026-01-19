package com.todaktodot.TDTD.domain.feedback.dto.response;

import com.todaktodot.TDTD.domain.feedback.repository.entity.DailyCardFeedbackEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "AI 피드백 생성 응답")
public class GenerateFeedbackResponseDTO {

    @Schema(description = "피드백 ID", example = "10")
    private final Long feedbackId;

    @Schema(description = "요약")
    private final String summary;

    @Schema(description = "공감 포인트")
    private final String matchPoints;

    @Schema(description = "차이점")
    private final String differences;

    @Schema(description = "대화 시작 질문")
    private final String conversationStarter;

    public GenerateFeedbackResponseDTO(Long feedbackId, String summary, String matchPoints,
                                       String differences, String conversationStarter) {
        this.feedbackId = feedbackId;
        this.summary = summary;
        this.matchPoints = matchPoints;
        this.differences = differences;
        this.conversationStarter = conversationStarter;
    }

    public static GenerateFeedbackResponseDTO from(DailyCardFeedbackEntity feedback) {
        return new GenerateFeedbackResponseDTO(
                feedback.getFeedbackId(),
                feedback.getSummary(),
                feedback.getMatchPoints(),
                feedback.getDifferences(),
                feedback.getConversationStarter()
        );
    }
}
