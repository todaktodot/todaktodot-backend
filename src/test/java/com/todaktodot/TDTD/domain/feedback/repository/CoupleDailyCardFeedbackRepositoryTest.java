package com.todaktodot.TDTD.domain.feedback.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.todaktodot.TDTD.domain.feedback.repository.entity.CoupleDailyCardFeedbackEntity;
import com.todaktodot.TDTD.domain.feedback.repository.entity.FeedbackGenerationStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("커플 데일리카드 피드백 매핑 리포지토리 테스트")
class CoupleDailyCardFeedbackRepositoryTest {

    @Autowired
    private CoupleDailyCardFeedbackRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("NOT_STARTED -> GENERATING 조건부 전이는 한 번만 성공한다")
    void transitionNotStartedToGenerating_SucceedsOnlyOnce() {
        CoupleDailyCardFeedbackEntity mapping = saveMapping(null);

        int firstUpdated = repository.transitionNotStartedToGenerating(
                mapping.getCoupleCardId(),
                2L,
                FeedbackGenerationStatus.NOT_STARTED,
                FeedbackGenerationStatus.GENERATING,
                LocalDateTime.now());
        int secondUpdated = repository.transitionNotStartedToGenerating(
                mapping.getCoupleCardId(),
                3L,
                FeedbackGenerationStatus.NOT_STARTED,
                FeedbackGenerationStatus.GENERATING,
                LocalDateTime.now());

        entityManager.clear();
        CoupleDailyCardFeedbackEntity result = repository.findByCoupleCardIdAndDelYn(mapping.getCoupleCardId(), "N")
                .orElseThrow();

        assertThat(firstUpdated).isEqualTo(1);
        assertThat(secondUpdated).isZero();
        assertThat(result.getStatus()).isEqualTo(FeedbackGenerationStatus.GENERATING);
        assertThat(result.getFeedbackId()).isNull();
        assertThat(result.getUpdrId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("GENERATING -> COMPLETED 이후 FAILED 조건부 전이는 실패한다")
    void markCompletedPreventsLaterFailedTransition() {
        CoupleDailyCardFeedbackEntity mapping = saveGeneratingMapping();

        int completedUpdated = repository.markCompletedIfGenerating(
                mapping.getCoupleCardId(),
                100L,
                2L,
                FeedbackGenerationStatus.GENERATING,
                FeedbackGenerationStatus.COMPLETED,
                LocalDateTime.now());
        int failedUpdated = repository.markFailedIfGenerating(
                mapping.getCoupleCardId(),
                "AI error",
                3L,
                FeedbackGenerationStatus.GENERATING,
                FeedbackGenerationStatus.FAILED,
                LocalDateTime.now());

        entityManager.clear();
        CoupleDailyCardFeedbackEntity result = repository.findByCoupleCardIdAndDelYn(mapping.getCoupleCardId(), "N")
                .orElseThrow();

        assertThat(completedUpdated).isEqualTo(1);
        assertThat(failedUpdated).isZero();
        assertThat(result.getStatus()).isEqualTo(FeedbackGenerationStatus.COMPLETED);
        assertThat(result.getFeedbackId()).isEqualTo(100L);
        assertThat(result.getErrorMessage()).isNull();
        assertThat(result.getUpdrId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("GENERATING -> FAILED 이후 COMPLETED 조건부 전이는 실패한다")
    void markFailedPreventsLaterCompletedTransition() {
        CoupleDailyCardFeedbackEntity mapping = saveGeneratingMapping();

        int failedUpdated = repository.markFailedIfGenerating(
                mapping.getCoupleCardId(),
                "AI error",
                2L,
                FeedbackGenerationStatus.GENERATING,
                FeedbackGenerationStatus.FAILED,
                LocalDateTime.now());
        int completedUpdated = repository.markCompletedIfGenerating(
                mapping.getCoupleCardId(),
                100L,
                3L,
                FeedbackGenerationStatus.GENERATING,
                FeedbackGenerationStatus.COMPLETED,
                LocalDateTime.now());

        entityManager.clear();
        CoupleDailyCardFeedbackEntity result = repository.findByCoupleCardIdAndDelYn(mapping.getCoupleCardId(), "N")
                .orElseThrow();

        assertThat(failedUpdated).isEqualTo(1);
        assertThat(completedUpdated).isZero();
        assertThat(result.getStatus()).isEqualTo(FeedbackGenerationStatus.FAILED);
        assertThat(result.getFeedbackId()).isNull();
        assertThat(result.getErrorMessage()).isEqualTo("AI error");
        assertThat(result.getUpdrId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("COMPLETED 매핑은 GENERATING으로 되돌릴 수 없다")
    void transitionCompletedToGenerating_Fails() {
        CoupleDailyCardFeedbackEntity mapping = saveMapping(100L);

        int updated = repository.transitionNotStartedToGenerating(
                mapping.getCoupleCardId(),
                2L,
                FeedbackGenerationStatus.NOT_STARTED,
                FeedbackGenerationStatus.GENERATING,
                LocalDateTime.now());

        entityManager.clear();
        CoupleDailyCardFeedbackEntity result = repository.findByCoupleCardIdAndDelYn(mapping.getCoupleCardId(), "N")
                .orElseThrow();

        assertThat(updated).isZero();
        assertThat(result.getStatus()).isEqualTo(FeedbackGenerationStatus.COMPLETED);
        assertThat(result.getFeedbackId()).isEqualTo(100L);
    }

    private CoupleDailyCardFeedbackEntity saveGeneratingMapping() {
        CoupleDailyCardFeedbackEntity mapping = CoupleDailyCardFeedbackEntity.builder()
                .coupleCardId(System.nanoTime())
                .regrId(1L)
                .updrId(1L)
                .build();
        mapping.markGenerating(1L);
        return repository.saveAndFlush(mapping);
    }

    private CoupleDailyCardFeedbackEntity saveMapping(Long feedbackId) {
        CoupleDailyCardFeedbackEntity mapping = CoupleDailyCardFeedbackEntity.builder()
                .coupleCardId(System.nanoTime())
                .feedbackId(feedbackId)
                .regrId(1L)
                .updrId(1L)
                .build();
        return repository.saveAndFlush(mapping);
    }
}
