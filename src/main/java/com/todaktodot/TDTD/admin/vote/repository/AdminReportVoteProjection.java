package com.todaktodot.TDTD.admin.vote.repository;

import java.time.LocalDateTime;

public interface AdminReportVoteProjection {
    Long getVoteId();
    String getTitle();
    Integer getReportedCnt();
    String getStatus();
    LocalDateTime getRegDt();
}
