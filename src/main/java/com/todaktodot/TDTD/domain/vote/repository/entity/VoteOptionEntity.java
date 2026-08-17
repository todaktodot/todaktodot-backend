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
@Table(name = "VOTE_OPTION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VoteOptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OPTION_ID")
    private Long optionId;

    @Column(name = "VOTE_ID", nullable = false)
    private Long voteId;

    // 표시 순서(등록 순). ORDER 가 SQL 예약어라 컬럼명은 SORT_ORDER 를 쓴다.
    @Column(name = "SORT_ORDER", nullable = false)
    private Integer sortOrder;

    @Column(name = "CONTENT", nullable = false, length = 20)
    private String content;

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
    public VoteOptionEntity(Long voteId, Integer sortOrder, String content, Long regrId) {
        this.voteId = voteId;
        this.sortOrder = sortOrder;
        this.content = content;
        this.regrId = regrId;
        this.updrId = regrId;
        this.delYn = "N";
    }
}
