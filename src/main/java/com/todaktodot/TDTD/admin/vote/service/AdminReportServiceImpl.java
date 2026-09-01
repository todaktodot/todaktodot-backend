package com.todaktodot.TDTD.admin.vote.service;

import com.todaktodot.TDTD.admin.vote.dto.AdminReportDetailDTO;
import com.todaktodot.TDTD.admin.vote.dto.AdminReportListDTO;
import com.todaktodot.TDTD.admin.vote.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminReportServiceImpl implements AdminReportService{

    private final AdminReportRepository reportRepository;
    @Override
    public Page<AdminReportListDTO> getReports(String resolvedStatus, String resolvedSortBy, String keyword, Pageable pageable) {
        Page<AdminReportProjection> allReportedUsers = reportRepository.findAllReportedUsers(resolvedStatus, resolvedSortBy, keyword, pageable);

        return allReportedUsers.map(AdminReportListDTO::from);
    }

    @Override
    public Integer getTotalReportUserCount() {
        return reportRepository.countReportedUsers();
    }

    @Override
    public Integer getNormalUserCount() {
        return reportRepository.countNormalUsers();
    }

    @Override
    public Integer getSuspendedUserCount() {
        return reportRepository.countSuspendedUsers();
    }

    @Override
    public Integer getWeeklySuspendedUserCount() {
        return reportRepository.countWeeklySuspendedUsers();
    }

    @Override
    public AdminReportDetailDTO getDetail(Long userId) {
        AdminReportDetailProjection reportDetail = reportRepository.findReportDetail(userId);
        List<AdminReportVoteProjection> reportedVotes = reportRepository.findReportedVotes(userId);
        AdminSuspensionProjection activeSuspension = reportRepository.findActiveSuspensionByUserId(userId);
        AdminSuspensionProjection activeRelease = reportRepository.findActiveReleaseByUserId(userId);

        return AdminReportDetailDTO.from(reportDetail, reportedVotes, activeSuspension, activeRelease);
    }
}
