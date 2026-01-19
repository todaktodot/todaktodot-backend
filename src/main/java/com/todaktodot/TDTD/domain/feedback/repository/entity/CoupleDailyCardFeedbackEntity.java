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
@Table(name = "COUPLE_DAILY_CARD_FEEDBACK")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CoupleDailyCardFeedbackEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MAPPING_ID")
    private Long mappingId;

    @Column(name = "COUPLE_CARD_ID", nullable = false)
    private Long coupleCardId;

    @Column(name = "FEEDBACK_ID", nullable = false)
    private Long feedbackId;

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
    public CoupleDailyCardFeedbackEntity(Long coupleCardId, Long feedbackId, Long regrId, Long updrId) {
        this.coupleCardId = coupleCardId;
        this.feedbackId = feedbackId;
        this.regrId = regrId;
        this.updrId = updrId;
        this.delYn = "N";
    }

    public void updateFeedback(Long feedbackId, Long updrId) {
        this.feedbackId = feedbackId;
        this.updrId = updrId;
    }
}
