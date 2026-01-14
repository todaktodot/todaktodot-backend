package com.todaktodot.TDTD.domain.aireport.service;

import com.todaktodot.TDTD.domain.aireport.dto.response.ReportResponseWrapDTO;

public interface ReportService {
    ReportResponseWrapDTO checkCreatable(Long userId);
}
