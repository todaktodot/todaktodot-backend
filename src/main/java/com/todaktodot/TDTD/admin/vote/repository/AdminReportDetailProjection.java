package com.todaktodot.TDTD.admin.vote.repository;

import java.time.LocalDateTime;

public interface AdminReportDetailProjection {
    Long getUserId();
    Integer getVoteCnt();
    Integer getReportedCnt();
    Integer getDeletedCnt();
    Integer getAutoHiddenCnt();
    LocalDateTime getLatestReportDt();
    LocalDateTime getSuspendedDt();
    LocalDateTime getJoinedDt();
}
