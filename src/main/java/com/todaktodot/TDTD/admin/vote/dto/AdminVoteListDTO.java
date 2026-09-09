package com.todaktodot.TDTD.admin.vote.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AdminVoteListDTO {

    private final Long voteId;
    private final String title;
    private final String categoryLabel;
    private final String randomNickname;
    private final Integer participantCnt;
    private final Long likeCnt;
    private final Long reportCnt;
    private final String voteStatusCode;   // ACTIVE, CLOSED, AUTO_HIDDEN, HIDDEN
    private final String voteStatusLabel;  // 진행중, 마감, 자동숨김, 숨김
    private final String reportStatusCode;  // NONE, PENDING, RESOLVED
    private final String reportStatusLabel; // 신고 없음, 검토 필요, 검토 완료
    private final String waitingDisplay;    // 대기 경과, PENDING 이 아니면 null
    private final boolean waitingOverdue;   // 3일 초과 강조 여부
    private final LocalDateTime regDt;

    public AdminVoteListDTO(Long voteId, String title, String categoryLabel, String randomNickname,
                             Integer participantCnt, Long likeCnt, Long reportCnt,
                             String voteStatusCode, String voteStatusLabel,
                             String reportStatusCode, String reportStatusLabel,
                             String waitingDisplay, boolean waitingOverdue, LocalDateTime regDt) {
        this.voteId = voteId;
        this.title = title;
        this.categoryLabel = categoryLabel;
        this.randomNickname = randomNickname;
        this.participantCnt = participantCnt;
        this.likeCnt = likeCnt;
        this.reportCnt = reportCnt;
        this.voteStatusCode = voteStatusCode;
        this.voteStatusLabel = voteStatusLabel;
        this.reportStatusCode = reportStatusCode;
        this.reportStatusLabel = reportStatusLabel;
        this.waitingDisplay = waitingDisplay;
        this.waitingOverdue = waitingOverdue;
        this.regDt = regDt;
    }

    public boolean isHideButtonVisible() {
        return "ACTIVE".equals(voteStatusCode) || "CLOSED".equals(voteStatusCode);
    }

    public boolean isRestoreButtonVisible() {
        return "AUTO_HIDDEN".equals(voteStatusCode) || "HIDDEN".equals(voteStatusCode);
    }
}
