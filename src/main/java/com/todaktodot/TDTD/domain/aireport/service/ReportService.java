package com.todaktodot.TDTD.domain.aireport.service;

import com.todaktodot.TDTD.domain.aireport.dto.response.ReportCreateStatusResponseDTO;
import com.todaktodot.TDTD.domain.aireport.dto.response.ReportDetailResponseDTO;
import com.todaktodot.TDTD.domain.aireport.dto.response.ReportListResponseDTO;
import com.todaktodot.TDTD.domain.aireport.dto.response.ReportResponseWrapDTO;
import com.todaktodot.TDTD.domain.aireport.repository.entity.Report;
import com.todaktodot.TDTD.domain.couple.repository.entity.CoupleEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ReportService {
    ReportCreateStatusResponseDTO checkCreatable(Long userId);
    Report createReport(CoupleEntity coupleEntity, Long insightId, LocalDate startD, LocalDate endD);

    List<ReportListResponseDTO> getReportList(Long id);

    ReportDetailResponseDTO getReportDetail(Long id, Long reportId);
}
