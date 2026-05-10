package com.todaktodot.TDTD.domain.feedback.repository;

import com.todaktodot.TDTD.domain.feedback.repository.entity.CoupleDailyCardFeedbackEntity;
import com.todaktodot.TDTD.domain.feedback.repository.entity.FeedbackGenerationStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CoupleDailyCardFeedbackRepository extends JpaRepository<CoupleDailyCardFeedbackEntity, Long> {

    Optional<CoupleDailyCardFeedbackEntity> findByCoupleCardIdAndDelYn(Long coupleCardId, String delYn);

    List<CoupleDailyCardFeedbackEntity> findAllByCoupleCardIdInAndDelYn(List<Long> coupleCardIds, String delYn);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE CoupleDailyCardFeedbackEntity mapping
               SET mapping.status = :generatingStatus,
                   mapping.feedbackId = null,
                   mapping.errorMessage = null,
                   mapping.startedAt = :now,
                   mapping.completedAt = null,
                   mapping.updDt = :now,
                   mapping.updrId = :userId
             WHERE mapping.coupleCardId = :coupleCardId
               AND mapping.status = :notStartedStatus
               AND mapping.delYn = 'N'
            """)
    int transitionNotStartedToGenerating(
            @Param("coupleCardId") Long coupleCardId,
            @Param("userId") Long userId,
            @Param("notStartedStatus") FeedbackGenerationStatus notStartedStatus,
            @Param("generatingStatus") FeedbackGenerationStatus generatingStatus,
            @Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE CoupleDailyCardFeedbackEntity mapping
               SET mapping.status = :completedStatus,
                   mapping.feedbackId = :feedbackId,
                   mapping.errorMessage = null,
                   mapping.completedAt = :now,
                   mapping.updDt = :now,
                   mapping.updrId = :userId
             WHERE mapping.coupleCardId = :coupleCardId
               AND mapping.status = :generatingStatus
               AND mapping.delYn = 'N'
            """)
    int markCompletedIfGenerating(
            @Param("coupleCardId") Long coupleCardId,
            @Param("feedbackId") Long feedbackId,
            @Param("userId") Long userId,
            @Param("generatingStatus") FeedbackGenerationStatus generatingStatus,
            @Param("completedStatus") FeedbackGenerationStatus completedStatus,
            @Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE CoupleDailyCardFeedbackEntity mapping
               SET mapping.status = :failedStatus,
                   mapping.feedbackId = null,
                   mapping.errorMessage = :errorMessage,
                   mapping.completedAt = :now,
                   mapping.updDt = :now,
                   mapping.updrId = :userId
             WHERE mapping.coupleCardId = :coupleCardId
               AND mapping.status = :generatingStatus
               AND mapping.delYn = 'N'
            """)
    int markFailedIfGenerating(
            @Param("coupleCardId") Long coupleCardId,
            @Param("errorMessage") String errorMessage,
            @Param("userId") Long userId,
            @Param("generatingStatus") FeedbackGenerationStatus generatingStatus,
            @Param("failedStatus") FeedbackGenerationStatus failedStatus,
            @Param("now") LocalDateTime now);
}
