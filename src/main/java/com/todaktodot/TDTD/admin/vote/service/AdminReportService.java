package com.todaktodot.TDTD.admin.vote.service;

import com.todaktodot.TDTD.admin.vote.dto.AdminReportDetailDTO;
import com.todaktodot.TDTD.admin.vote.dto.AdminReportListDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminReportService {
    Page<AdminReportListDTO> getReports(String resolvedStatus, String resolvedSortBy, String keyword, Pageable pageable);

    Integer getTotalReportUserCount();

    Integer getNormalUserCount();

    Integer getSuspendedUserCount();

    Integer getWeeklySuspendedUserCount();

    AdminReportDetailDTO getDetail(Long userId);
}
