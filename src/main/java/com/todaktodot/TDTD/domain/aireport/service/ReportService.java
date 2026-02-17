package com.todaktodot.TDTD.domain.aireport.service;

import com.todaktodot.TDTD.domain.aireport.dto.response.ReportCreateStatusResponseDTO;
import com.todaktodot.TDTD.domain.aireport.dto.response.ReportDetailResponseDTO;
import com.todaktodot.TDTD.domain.aireport.dto.response.ReportListResponseDTO;
import com.todaktodot.TDTD.domain.aireport.dto.response.ReportResponseWrapDTO;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    ReportCreateStatusResponseDTO checkCreatable(Long userId);

    List<ReportListResponseDTO> getReportList(Long id);

    ReportDetailResponseDTO getReportDetail(Long id, Long reportId);
}
