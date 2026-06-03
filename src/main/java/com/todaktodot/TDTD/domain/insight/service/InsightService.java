package com.todaktodot.TDTD.domain.insight.service;

import com.todaktodot.TDTD.domain.insight.dto.GenerateInsightRequestDTO;
import com.todaktodot.TDTD.domain.insight.dto.GenerateInsightResponseDTO;

public interface InsightService {

    /**
     * AI 인사이트 생성
     */
    GenerateInsightResponseDTO generateInsight(GenerateInsightRequestDTO requestDTO);
}
