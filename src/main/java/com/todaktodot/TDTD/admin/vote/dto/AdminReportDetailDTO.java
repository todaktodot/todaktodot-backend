package com.todaktodot.TDTD.admin.vote.dto;

import com.todaktodot.TDTD.admin.vote.repository.AdminReportDetailProjection;
import com.todaktodot.TDTD.admin.vote.repository.AdminReportProjection;
import com.todaktodot.TDTD.admin.vote.repository.AdminReportVoteProjection;
import com.todaktodot.TDTD.admin.vote.repository.AdminSuspensionProjection;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
public class AdminReportDetailDTO {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final Long userId;
    private final String regDate;
    private final Integer createdVoteCnt;
    private final Integer reportedCnt;
    private final Integer deletedCnt;
    private final Integer autoHiddenCnt;
    private final LocalDateTime latestReportDt;
    private final String status;
    private final LocalDateTime suspendStartDt;
    private final String suspendPeriod;
    private final List<ReportedVote> reportedVoteList;
    private final SuspendHistory suspendHistory;
    private final ReleaseHistory releaseHistory;

    public AdminReportDetailDTO(Long userId, String regDate, Integer createdVoteCnt, Integer reportedCnt, Integer deletedCnt, Integer autoHiddenCnt, LocalDateTime latestReportDt, String status, LocalDateTime suspendStartDt, String suspendPeriod, List<ReportedVote> reportedVoteList, SuspendHistory suspendHistory, ReleaseHistory releaseHistory) {
        this.userId = userId;
        this.regDate = regDate;
        this.createdVoteCnt = createdVoteCnt;
        this.reportedCnt = reportedCnt;
        this.deletedCnt = deletedCnt;
        this.autoHiddenCnt = autoHiddenCnt;
        this.latestReportDt = latestReportDt;
        this.status = status;
        this.suspendStartDt = suspendStartDt;
        this.suspendPeriod = suspendPeriod;
        this.reportedVoteList = reportedVoteList;
        this.suspendHistory = suspendHistory;
        this.releaseHistory = releaseHistory;
    }

    public static AdminReportDetailDTO from(AdminReportDetailProjection reportDetail,
                                            List<AdminReportVoteProjection> reportedVotes,
                                            AdminSuspensionProjection activeSuspension,
                                            AdminSuspensionProjection activeRelease) {
        List<ReportedVote> reportedVoteList = reportedVotes.stream()
                .map(ReportedVote::from)
                .toList();

        SuspendHistory suspendHistory = activeSuspension != null ? SuspendHistory.from(activeSuspension) : null;
        ReleaseHistory releaseHistory = activeRelease != null ? ReleaseHistory.from(activeRelease) : null;

        return new AdminReportDetailDTO(
                reportDetail.getUserId(),
                formatDate(reportDetail.getJoinedDt()),
                reportDetail.getVoteCnt(),
                reportDetail.getReportedCnt(),
                reportDetail.getDeletedCnt(),
                reportDetail.getAutoHiddenCnt(),
                reportDetail.getLatestReportDt(),
                reportDetail.getSuspendedDt() != null ? "SUSPENDED" : "NORMAL",
                reportDetail.getSuspendedDt(),
                "무기한",
                reportedVoteList,
                suspendHistory,
                releaseHistory
        );
    }

    private static String formatDate(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_FORMATTER) : "-";
    }

    @Getter
    public static class ReportedVote {
        private final long voteId;
        private final String title;
        private final Integer reportedCnt;
        private final String status;

        public ReportedVote(long voteId, String title, Integer reportedCnt, String status) {
            this.voteId = voteId;
            this.title = title;
            this.reportedCnt = reportedCnt;
            this.status = status;
        }

        public static ReportedVote from(AdminReportVoteProjection projection) {
            return new ReportedVote(
                    projection.getVoteId(),
                    projection.getTitle(),
                    projection.getReportedCnt(),
                    projection.getStatus()
            );
        }
    }

    @Getter
    public static class SuspendHistory {
        private final String reason;
        private final String adminNm;
        private final LocalDateTime suspendDt;

        public SuspendHistory(String suspendReason, String adminNm, LocalDateTime suspendDt) {
            this.reason = suspendReason;
            this.adminNm = adminNm;
            this.suspendDt = suspendDt;
        }

        public static SuspendHistory from(AdminSuspensionProjection projection) {
            return new SuspendHistory(projection.getReason(), "ADMIN", projection.getRegDt());
        }
    }

    @Getter
    public static class ReleaseHistory {
        private final String reason;
        private final String adminNm;
        private final LocalDateTime releaseDt;

        public ReleaseHistory(String reason, String adminNm, LocalDateTime releaseDt) {
            this.reason = reason;
            this.adminNm = adminNm;
            this.releaseDt = releaseDt;
        }

        public static ReleaseHistory from(AdminSuspensionProjection projection) {
            return new ReleaseHistory(projection.getReason(), "ADMIN", projection.getRegDt());
        }
    }
}
