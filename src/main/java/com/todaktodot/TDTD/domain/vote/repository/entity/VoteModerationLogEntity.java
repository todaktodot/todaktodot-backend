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
@Table(name = "vote_moderation_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VoteModerationLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LOG_ID")
    private Long logId;

    @Column(name = "VOTE_ID", nullable = false)
    private Long voteId;

    // 변경 전 상태. 최초 등록 시 NULL
    @Column(name = "PREV_STATUS", length = 20)
    private String prevStatus;

    @Column(name = "NEW_STATUS", nullable = false, length = 20)
    private String newStatus;

    // system(자동 숨김) 또는 관리자 계정명
    @Column(name = "ACTOR", nullable = false, length = 50)
    private String actor;

    @Column(name = "MEMO", length = 200)
    private String memo;

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
    public VoteModerationLogEntity(Long voteId, String prevStatus, String newStatus, String actor,
                                    String memo, Long regrId) {
        this.voteId = voteId;
        this.prevStatus = prevStatus;
        this.newStatus = newStatus;
        this.actor = actor;
        this.memo = memo;
        this.regrId = regrId;
        this.updrId = regrId;
        this.delYn = "N";
    }
}
