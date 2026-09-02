package com.todaktodot.TDTD.admin.vote.service;

import com.todaktodot.TDTD.admin.vote.dto.AdminReleaseRequestDTO;
import com.todaktodot.TDTD.admin.vote.dto.AdminReportDetailDTO;
import com.todaktodot.TDTD.admin.vote.dto.AdminReportListDTO;
import com.todaktodot.TDTD.admin.vote.dto.AdminSuspendRequestDTO;
import com.todaktodot.TDTD.admin.vote.repository.*;
import com.todaktodot.TDTD.domain.vote.repository.entity.SuspensionStatus;
import com.todaktodot.TDTD.domain.vote.repository.entity.UserSuspensionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminReportServiceImpl implements AdminReportService{

    private final AdminReportRepository reportRepository;
    private final AdminSuspensionRepository suspensionRepository;
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

    @Override
    @Transactional
    public void suspend(AdminSuspendRequestDTO request) {
        // 1. 이미 현재 정지 중인지 확인
        boolean alreadySuspended = suspensionRepository.existsByUserIdAndStatusAndDelYn(request.getUserId(), SuspensionStatus.SUSPENDED, "N");

        if (alreadySuspended) {
            throw new IllegalStateException("이미 정지된 사용자입니다.");
        }

        // 2. 기존 해제 이력 논리 삭제
        suspensionRepository.softDeleteHistory(request.getUserId(), SuspensionStatus.RELEASED , 0L);

        // 3. 신규 정지 이력 생성
        UserSuspensionEntity suspension = new UserSuspensionEntity(request.getUserId(),SuspensionStatus.SUSPENDED, request.getType(), request.getReason(), LocalDateTime.now(), 0L);

        suspensionRepository.save(suspension);
    }

    @Override
    @Transactional
    public void release(AdminReleaseRequestDTO request) {
        // 1. 현재 정지 중인지 확인
        boolean alreadySuspended = suspensionRepository.existsByUserIdAndStatusAndDelYn(request.getUserId(), SuspensionStatus.SUSPENDED, "N");

        if (!alreadySuspended) {
            throw new IllegalStateException("정지되지 않은 사용자입니다.");
        }

        // 2. 이미 현재 해제 중인지 확인
        boolean alreadyReleased = suspensionRepository.existsByUserIdAndStatusAndDelYn(request.getUserId(), SuspensionStatus.RELEASED, "N");

        if (alreadyReleased) {
            throw new IllegalStateException("이미 해제된 사용자입니다.");
        }

        // 3. 기존 정지 이력 논리 삭제
        suspensionRepository.softDeleteHistory(request.getUserId(), SuspensionStatus.SUSPENDED , 0L);

        // 4. 신규 해제 이력 생성
        UserSuspensionEntity release = new UserSuspensionEntity(request.getUserId(),SuspensionStatus.RELEASED, request.getType(), request.getReason(), LocalDateTime.now(), 0L);

        suspensionRepository.save(release);
    }
}
