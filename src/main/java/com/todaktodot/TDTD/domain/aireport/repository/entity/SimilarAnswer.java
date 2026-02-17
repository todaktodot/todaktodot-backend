package com.todaktodot.TDTD.domain.aireport.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SimilarAnswer {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SIMILAR_ANSWER_ID")
    private Long id;

    @Column(name = "ANSWER_ID_1")
    private Long answerId1;

    @Column(name = "ANSWER_ID_2")
    private Long answerId2;

    @Column(name = "COUPLE_CARD_ID", nullable = false)
    private Long coupleCardId;

//    @Column(name = "CARD_ID", nullable = false)
//    private Long cardId;

    @Column(name = "REG_DT", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime regDt;

    @Column(name = "REGR_ID", nullable = false, columnDefinition = "BIGINT")
    private Long regrId;

    @Column(name = "UPD_DT", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updDt;

    @Column(name = "UPDR_ID", nullable = false, columnDefinition = "BIGINT")
    private Long updrId;

    @Column(name = "DEL_YN", nullable = false, length = 1)
    @Builder.Default
    private String delYn = "N";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REPORT_ID")
    private Report report;

    @Builder
    public SimilarAnswer(Long answerId1, Long answerId2, Long coupleCardId, Long regrId, Long updrId, String delYn, Report report) {
        this.answerId1 = answerId1;
        this.answerId2 = answerId2;
        this.coupleCardId = coupleCardId;
        this.regrId = regrId;
        this.updrId = updrId;
        this.delYn = delYn;
        this.report = report;
    }

    public void addReport(Report report) {
        this.report = report;
    }
}
