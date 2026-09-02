package com.todaktodot.TDTD.admin.vote.dto;

import com.todaktodot.TDTD.admin.vote.repository.AdminReportProjection;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class AdminReportListDTO {

    private final Long userId;
    private final Integer reportedCnt;
    private final Integer deletedCnt;
    private final Integer autoHiddenCnt;
    private final String latestReportDt;
    private final String status;

    public AdminReportListDTO(Long userId, Integer reportedCnt, Integer deletedCnt, Integer autoHiddenCnt, String latestReportDt, String status) {
        this.userId = userId;
        this.reportedCnt = reportedCnt;
        this.deletedCnt = deletedCnt;
        this.autoHiddenCnt = autoHiddenCnt;
        this.latestReportDt = latestReportDt;
        this.status = status;
    }

    public boolean isSuspendButtonVisible() {
        return "NORMAL".equals(status);
    }

    public boolean isReleaseButtonVisible() {
        return "SUSPENDED".equals(status);
    }

    public static AdminReportListDTO from(AdminReportProjection projection) {
        return new AdminReportListDTO(projection.getUserId(),
                projection.getReportedCnt(),
                projection.getDeletedCnt(),
                projection.getAutoHiddenCnt(),
                projection.getLatestReportDt() != null
                        ? projection.getLatestReportDt()
                        .format(DateTimeFormatter.ofPattern("MM-dd"))
                        : "-",
                projection.getStatus()
        );
    }

}
