package com.todaktodot.TDTD.admin.vote.service;

import com.todaktodot.TDTD.admin.vote.dto.AdminReleaseRequestDTO;
import com.todaktodot.TDTD.admin.vote.dto.AdminReportDetailDTO;
import com.todaktodot.TDTD.admin.vote.dto.AdminReportListDTO;
import com.todaktodot.TDTD.admin.vote.dto.AdminSuspendRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminReportService {
    /**
     * 신고 목록 조회
     * @param resolvedStatus 상태
     * @param resolvedSortBy 정렬
     * @param keyword 검색어
     * @param pageable 페이징
     */
    Page<AdminReportListDTO> getReports(String resolvedStatus, String resolvedSortBy, String keyword, Pageable pageable);

    /**
     * 신고당한 총 사용자 수
     */
    Integer getTotalReportUserCount();

    /**
     * 신고 당한 사용자 중 활성중인 수
     */
    Integer getNormalUserCount();

    /**
     * 정지된 사용자 수
     */
    Integer getSuspendedUserCount();

    /**
     * 금주 정지당한 사용자 수
     */
    Integer getWeeklySuspendedUserCount();

    AdminReportDetailDTO getDetail(Long userId);

    void suspend(AdminSuspendRequestDTO request);

    void release(AdminReleaseRequestDTO request);
}
