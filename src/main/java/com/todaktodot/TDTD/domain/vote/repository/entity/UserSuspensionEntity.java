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
@Table(name = "USER_SUSPENSION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSuspensionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SUSPENSION_ID")
    private Long suspensionId;

    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private SuspensionStatus status = SuspensionStatus.SUSPENDED;

    @Column(name = "SUSPENDED_DT", nullable = false)
    private LocalDateTime suspendedDt;

    @Column(name = "RELEASED_DT")
    private LocalDateTime releasedDt;

    @Column(name = "NOTICE_ACK_YN", nullable = false, length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String noticeAckYn = "N";

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

    // DB 생성 컬럼(STATUS='SUSPENDED' AND DEL_YN='N' 이면 1, 아니면 NULL). 읽기 전용으로 매핑한다.
    @Column(name = "ACTIVE_SLOT", insertable = false, updatable = false)
    private Integer activeSlot;

    @Builder
    public UserSuspensionEntity(Long userId, LocalDateTime suspendedDt, Long regrId) {
        this.userId = userId;
        this.suspendedDt = suspendedDt;
        this.status = SuspensionStatus.SUSPENDED;
        this.noticeAckYn = "N";
        this.regrId = regrId;
        this.updrId = regrId;
        this.delYn = "N";
    }
}
