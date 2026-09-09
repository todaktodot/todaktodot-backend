package com.todaktodot.TDTD.domain.vote.repository.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "VOTE_REPORT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VoteReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REPORT_ID")
    private Long reportId;

    @Column(name = "VOTE_ID", nullable = false)
    private Long voteId;

    // 신고자. 작성자와 다른 유저에게는 공개하지 않는다.
    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "REASON", nullable = false, length = 20)
    private ReportReason reason;

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

    @Column(name = "DEL_YN", nullable = false, length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String delYn = "N";

    @Builder
    public VoteReportEntity(Long voteId, Long userId, ReportReason reason, Long regrId) {
        this.voteId = voteId;
        this.userId = userId;
        this.reason = reason;
        this.regrId = regrId;
        this.updrId = regrId;
        this.delYn = "N";
    }
}
