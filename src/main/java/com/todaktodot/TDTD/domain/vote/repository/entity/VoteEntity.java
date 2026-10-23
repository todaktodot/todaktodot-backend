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

    // 게시 시각 + 24시간. 수정해도 갱신하지 않는다.
    // DRAFT 는 아직 마감 시계가 돌면 안 되므로 NULL, 게시 시점에 채운다.
    @Column(name = "CLOSED_AT")
    private LocalDateTime closedAt;

    @Column(name = "PARTICIPANT_CNT", nullable = false)
    private Integer participantCnt = 0;

    @Column(name = "AUTO_HIDE_EXEMPT_YN", nullable = false, length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String autoHideExemptYn = "N";

    @Column(name = "HIDDEN_NOTICE_ACK_YN", nullable = false, length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String hiddenNoticeAckYn = "N";

    @Column(name = "REVIEW_CYCLE_STARTED_AT")
    private LocalDateTime reviewCycleStartedAt;

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

    // 초안 생성 - 어드민 승인 전 상태. 마감 시각은 게시 시점에 정한다
    public static VoteEntity draft(Long userId, String randomNickname, VoteCategory category,
                                   String title, Long regrId) {
        VoteEntity vote = new VoteEntity();
        vote.userId = userId;
        vote.randomNickname = randomNickname;
        vote.category = category;
        vote.title = title;
        vote.closedAt = null;
        vote.status = VoteDisplayStatus.DRAFT;
        vote.participantCnt = 0;
        vote.autoHideExemptYn = "N";
        vote.hiddenNoticeAckYn = "N";
        vote.regrId = regrId;
        vote.updrId = regrId;
        vote.delYn = "N";
        return vote;
    }

    public boolean isDraft() {
        return this.status == VoteDisplayStatus.DRAFT;
    }

    // 초안 게시 - 마감 시계는 이 시점부터 돈다
    public void publish(LocalDateTime closedAt, Long updrId) {
        if (!isDraft()) {
            throw new IllegalStateException("초안 상태의 투표만 게시할 수 있습니다.");
        }
        this.status = VoteDisplayStatus.POSTED;
        this.closedAt = closedAt;
        this.updrId = updrId;
        this.updDt = LocalDateTime.now();
    }

    public void updateVote(VoteCategory category, String title, Long userId) {
        this.category = category;
        this.title = title;
        this.updrId = userId;
        this.updDt = LocalDateTime.now();
    }

    public void deleteVote(Long userId) {
        this.delYn = "Y";
        this.updrId = userId;
        this.updDt = LocalDateTime.now();
    }

    //표출 상태 변경
    public void updateDisplayStatus(VoteDisplayStatus status, Long userId) {
        // 초안은 publish() 로만 게시한다
        if (isDraft() || status == VoteDisplayStatus.DRAFT) {
            throw new IllegalStateException("초안 상태는 이 방법으로 변경할 수 없습니다.");
        }
        this.status = status;
        this.updrId = userId;
        this.updDt = LocalDateTime.now();
    }

    // 신고 누적(자동) 또는 어드민 조치(수동)로 피드 비노출 처리
    public void hide(HideReason hideReason, Long updrId) {
        // 초안은 숨길 대상이 아니다
        if (isDraft()) {
            throw new IllegalStateException("초안은 숨길 수 없습니다.");
        }
        this.status = VoteDisplayStatus.HIDDEN;
        this.hideReason = hideReason;
        this.hiddenNoticeAckYn = "N";
        this.updrId = updrId;
    }

    // 복구 - 재노출 + 신고 검토 주기 초기화(이 시각 이전 신고는 신고건수 집계에서 제외)
    public void restore(Long updrId) {
        // 초안은 복구 대상이 아니다
        if (isDraft()) {
            throw new IllegalStateException("초안은 복구 대상이 아닙니다.");
        }
        this.status = VoteDisplayStatus.POSTED;
        this.hideReason = null;
        this.reviewCycleStartedAt = LocalDateTime.now();
        this.updrId = updrId;
    }
}
