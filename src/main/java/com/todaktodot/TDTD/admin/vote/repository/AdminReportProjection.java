package com.todaktodot.TDTD.admin.vote.repository;

import java.time.LocalDateTime;

public interface AdminReportProjection {
    Long getUserId();

    Integer getReportedCnt();

    Integer getDeletedCnt();

    Integer getAutoHiddenCnt();

    LocalDateTime getLatestReportDt();

    String getStatus();
}
