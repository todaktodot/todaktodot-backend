package com.todaktodot.TDTD.admin.vote.dto;

import lombok.Getter;

@Getter
public class AdminVoteStatsDTO {

    private final long totalCnt;
    private final long activeCnt;
    private final long closedCnt;
    private final long hiddenCnt;
    private final long reportPendingCnt;

    public AdminVoteStatsDTO(long totalCnt, long activeCnt, long closedCnt, long hiddenCnt, long reportPendingCnt) {
        this.totalCnt = totalCnt;
        this.activeCnt = activeCnt;
        this.closedCnt = closedCnt;
        this.hiddenCnt = hiddenCnt;
        this.reportPendingCnt = reportPendingCnt;
    }
}
