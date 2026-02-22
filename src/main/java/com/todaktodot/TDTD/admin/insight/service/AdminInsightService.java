package com.todaktodot.TDTD.admin.insight.service;

import com.todaktodot.TDTD.admin.insight.dto.InsightConfigDTO;
import com.todaktodot.TDTD.admin.prompt.dto.AiPromptDTO;

import java.util.List;
import java.util.Optional;

public interface AdminInsightService {

    Optional<InsightConfigDTO> getCurrentConfig();

    InsightConfigDTO saveConfig(InsightConfigDTO.SaveRequest request);

    List<AiPromptDTO> getSelectableInsightPrompts();

    Optional<AiPromptDTO> getActivePrompt();

    List<InsightConfigDTO> getConfigHistory();
}
