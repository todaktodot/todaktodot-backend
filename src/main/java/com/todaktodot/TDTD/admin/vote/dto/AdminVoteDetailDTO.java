package com.todaktodot.TDTD.admin.vote.dto;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class AdminVoteDetailDTO {

    private final Long voteId;
    private final String title;
    private final String categoryLabel;
    private final String voteStatusCode;
    private final String voteStatusLabel;
    private final String randomNickname;
    private final Long userId;
    private final LocalDateTime regDt;
    private final LocalDateTime closedAt;
    private final String remainingTimeDisplay;
    private final Integer participantCnt;
    private final Long likeCnt;
    private final Long reportCnt;
    private final Long deleteConfirmedCnt;
    private final List<OptionResult> optionResults;
    private final List<ReportRow> reports;
    private final List<ModerationLogRow> moderationLogs;

    public AdminVoteDetailDTO(Long voteId, String title, String categoryLabel, String voteStatusCode,
                               String voteStatusLabel, String randomNickname, Long userId, LocalDateTime regDt,
                               LocalDateTime closedAt, String remainingTimeDisplay, Integer participantCnt,
                               Long likeCnt, Long reportCnt, Long deleteConfirmedCnt, List<OptionResult> optionResults,
                               List<ReportRow> reports, List<ModerationLogRow> moderationLogs) {
        this.voteId = voteId;
        this.title = title;
        this.categoryLabel = categoryLabel;
        this.voteStatusCode = voteStatusCode;
        this.voteStatusLabel = voteStatusLabel;
        this.randomNickname = randomNickname;
        this.userId = userId;
        this.regDt = regDt;
        this.closedAt = closedAt;
        this.remainingTimeDisplay = remainingTimeDisplay;
        this.participantCnt = participantCnt;
        this.likeCnt = likeCnt;
        this.reportCnt = reportCnt;
        this.deleteConfirmedCnt = deleteConfirmedCnt;
        this.optionResults = optionResults;
        this.reports = reports;
        this.moderationLogs = moderationLogs;
    }

    public boolean isHideButtonVisible() {
        return "ACTIVE".equals(voteStatusCode) || "CLOSED".equals(voteStatusCode);
    }

    public boolean isRestoreButtonVisible() {
        return "AUTO_HIDDEN".equals(voteStatusCode) || "HIDDEN".equals(voteStatusCode);
    }

    @Getter
    public static class OptionResult {
        private final Long optionId;
        private final String content;
        private final long voteCnt;
        private final int ratePercent;
        private final boolean leading;

        public OptionResult(Long optionId, String content, long voteCnt, int ratePercent, boolean leading) {
            this.optionId = optionId;
            this.content = content;
            this.voteCnt = voteCnt;
            this.ratePercent = ratePercent;
            this.leading = leading;
        }
    }

    @Getter
    public static class ReportRow {
        private final Long userId;
        private final String nickname;
        private final String reasonLabel;
        private final LocalDateTime regDt;

        public ReportRow(Long userId, String nickname, String reasonLabel, LocalDateTime regDt) {
            this.userId = userId;
            this.nickname = nickname;
            this.reasonLabel = reasonLabel;
            this.regDt = regDt;
        }
    }

    @Getter
    public static class ModerationLogRow {
        private final LocalDateTime regDt;
        private final String actor;
        private final String description;

        public ModerationLogRow(LocalDateTime regDt, String actor, String description) {
            this.regDt = regDt;
            this.actor = actor;
            this.description = description;
        }
    }
}
