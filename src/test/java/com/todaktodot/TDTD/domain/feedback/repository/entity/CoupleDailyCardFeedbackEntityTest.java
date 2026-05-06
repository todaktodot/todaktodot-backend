package com.todaktodot.TDTD.domain.feedback.repository.entity;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class CoupleDailyCardFeedbackEntityTest {

    @Test
    void markGeneratingClearsResultAndError() {
        CoupleDailyCardFeedbackEntity mapping = CoupleDailyCardFeedbackEntity.builder()
                .coupleCardId(1L)
                .feedbackId(10L)
                .regrId(1L)
                .updrId(1L)
                .build();

        mapping.markGenerating(2L);

        assertThat(mapping.getFeedbackId()).isNull();
        assertThat(mapping.getStatus()).isEqualTo(FeedbackGenerationStatus.GENERATING);
        assertThat(mapping.getErrorMessage()).isNull();
        assertThat(mapping.getStartedAt()).isNotNull();
        assertThat(mapping.getCompletedAt()).isNull();
        assertThat(mapping.getUpdrId()).isEqualTo(2L);
    }

    @Test
    void updateFeedbackMarksCompleted() {
        CoupleDailyCardFeedbackEntity mapping = CoupleDailyCardFeedbackEntity.builder()
                .coupleCardId(1L)
                .regrId(1L)
                .updrId(1L)
                .build();

        mapping.updateFeedback(10L, 3L);

        assertThat(mapping.getFeedbackId()).isEqualTo(10L);
        assertThat(mapping.getStatus()).isEqualTo(FeedbackGenerationStatus.COMPLETED);
        assertThat(mapping.getErrorMessage()).isNull();
        assertThat(mapping.getCompletedAt()).isNotNull();
        assertThat(mapping.getUpdrId()).isEqualTo(3L);
    }
}
