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
@Table(name = "VOTE_LIKE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VoteLikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LIKE_ID")
    private Long likeId;

    @Column(name = "VOTE_ID", nullable = false)
    private Long voteId;

    @Column(name = "USER_ID", nullable = false)
    private Long userId;

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
    public VoteLikeEntity(Long voteId, Long userId, Long regrId) {
        this.voteId = voteId;
        this.userId = userId;
        this.regrId = regrId;
        this.updrId = regrId;
        this.delYn = "N";
    }

    // 좋아요 취소
    public void softDelete(Long updrId) {
        this.delYn = "Y";
        this.updrId = updrId;
    }
}
