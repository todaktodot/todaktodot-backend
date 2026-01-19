package com.todaktodot.TDTD.domain.feedback.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "DAILY_CARD_FEEDBACK")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyCardFeedbackEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FEEDBACK_ID")
    private Long feedbackId;

    @Column(name = "CARD_ID", nullable = false)
    private Long cardId;

    @Column(name = "CHOICE_COMBINATION_HASH", length = 64, nullable = false)
    private String choiceCombinationHash;

    @Column(name = "CHOICE_COMBINATION_RAW", columnDefinition = "TEXT", nullable = false)
    private String choiceCombinationRaw;

    @Column(name = "HAS_SUBJECTIVE", length = 1, nullable = false)
    private String hasSubjective;

    @Column(name = "SUMMARY", columnDefinition = "TEXT", nullable = false)
    private String summary;

    @Column(name = "MATCH_POINTS", columnDefinition = "TEXT", nullable = false)
    private String matchPoints;

    @Column(name = "DIFFERENCES", columnDefinition = "TEXT", nullable = false)
    private String differences;

    @Column(name = "CONVERSATION_STARTER", columnDefinition = "TEXT", nullable = false)
    private String conversationStarter;

    @CreationTimestamp
    @Column(name = "REG_DT", nullable = false, updatable = false)
    private LocalDateTime regDt;

    @Column(name = "REGR_ID", nullable = false)
    private Long regrId;

    @UpdateTimestamp
    @Column(name = "UPD_DT", nullable = false)
    private LocalDateTime updDt;

    @Column(name = "UPDR_ID", nullable = false)
    private Long updrId;

    @Column(name = "DEL_YN", length = 1, nullable = false)
    private String delYn = "N";

    @Builder
    public DailyCardFeedbackEntity(Long cardId, String choiceCombinationHash, String choiceCombinationRaw,
                                   String hasSubjective, String summary, String matchPoints,
                                   String differences, String conversationStarter, Long regrId, Long updrId) {
        this.cardId = cardId;
        this.choiceCombinationHash = choiceCombinationHash;
        this.choiceCombinationRaw = choiceCombinationRaw;
        this.hasSubjective = hasSubjective;
        this.summary = summary;
        this.matchPoints = matchPoints;
        this.differences = differences;
        this.conversationStarter = conversationStarter;
        this.regrId = regrId;
        this.updrId = updrId;
        this.delYn = "N";
    }
}
