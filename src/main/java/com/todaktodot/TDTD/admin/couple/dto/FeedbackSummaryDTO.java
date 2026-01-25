package com.todaktodot.TDTD.admin.couple.dto;

import com.todaktodot.TDTD.domain.feedback.repository.entity.DailyCardFeedbackEntity;
import lombok.Getter;

/**
 * Admin 커플 상세 화면에서 피드백 요약 정보를 표시하기 위한 DTO
 */
@Getter
public class FeedbackSummaryDTO {

    private final Long feedbackId;
    private final String summary;
    private final String matchPoints;
    private final String differences;
    private final String conversationStarter;

    public FeedbackSummaryDTO(Long feedbackId, String summary, String matchPoints,
                              String differences, String conversationStarter) {
        this.feedbackId = feedbackId;
        this.summary = summary;
        this.matchPoints = matchPoints;
        this.differences = differences;
        this.conversationStarter = conversationStarter;
    }

    public static FeedbackSummaryDTO from(DailyCardFeedbackEntity entity) {
        return new FeedbackSummaryDTO(
                entity.getFeedbackId(),
                entity.getSummary(),
                entity.getMatchPoints(),
                entity.getDifferences(),
                entity.getConversationStarter()
        );
    }
}
