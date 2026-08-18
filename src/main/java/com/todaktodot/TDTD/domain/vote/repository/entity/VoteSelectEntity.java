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
@Table(name = "VOTE_SELECT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VoteSelectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SELECT_ID")
    private Long selectId;

    @Column(name = "VOTE_ID", nullable = false)
    private Long voteId;

    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @Column(name = "OPTION_ID", nullable = false)
    private Long optionId;

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

    // DB 생성 컬럼(DEL_YN='N' 이면 1, 아니면 NULL). 유니크 제약에만 쓰이므로 읽기 전용으로 매핑한다.
    @Column(name = "ACTIVE_SLOT", insertable = false, updatable = false)
    private Integer activeSlot;

    @Builder
    public VoteSelectEntity(Long voteId, Long userId, Long optionId, Long regrId) {
        this.voteId = voteId;
        this.userId = userId;
        this.optionId = optionId;
        this.regrId = regrId;
        this.updrId = regrId;
        this.delYn = "N";
    }

    // 재투표 - 선택한 답변 항목 변경
    public void updateOptionId(Long optionId, Long updrId) {
        this.optionId = optionId;
        this.updrId = updrId;
    }

    // 투표 취소
    public void softDelete(Long updrId) {
        this.delYn = "Y";
        this.updrId = updrId;
    }
}
