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
@Table(name = "VOTE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "VOTE_ID")
    private Long voteId;

    // 작성자 식별용. 화면에는 노출하지 않고 randomNickname 을 표시한다.
    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @Column(name = "RANDOM_NICKNAME", nullable = false, length = 30)
    private String randomNickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "CATEGORY", nullable = false, length = 20)
    private VoteCategory category;

    @Column(name = "TITLE", nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private VoteDisplayStatus status = VoteDisplayStatus.POSTED;

    // STATUS 가 HIDDEN 일 때만 값이 존재한다.
    @Enumerated(EnumType.STRING)
    @Column(name = "HIDE_REASON", length = 20)
    private HideReason hideReason;

    // 등록 시각 + 24시간. 수정해도 갱신하지 않는다.
    @Column(name = "CLOSED_AT", nullable = false)
    private LocalDateTime closedAt;

    @Column(name = "PARTICIPANT_CNT", nullable = false)
    private Integer participantCnt = 0;

    @Column(name = "AUTO_HIDE_EXEMPT_YN", nullable = false, length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String autoHideExemptYn = "N";

    @Column(name = "HIDDEN_NOTICE_ACK_YN", nullable = false, length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String hiddenNoticeAckYn = "N";

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
    public VoteEntity(Long userId, String randomNickname, VoteCategory category, String title,
                      LocalDateTime closedAt, Long regrId) {
        this.userId = userId;
        this.randomNickname = randomNickname;
        this.category = category;
        this.title = title;
        this.closedAt = closedAt;
        this.status = VoteDisplayStatus.POSTED;
        this.participantCnt = 0;
        this.autoHideExemptYn = "N";
        this.hiddenNoticeAckYn = "N";
        this.regrId = regrId;
        this.updrId = regrId;
        this.delYn = "N";
    }
}
