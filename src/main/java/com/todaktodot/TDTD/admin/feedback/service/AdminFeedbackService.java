package com.todaktodot.TDTD.admin.feedback.service;

import com.todaktodot.TDTD.admin.feedback.dto.FeedbackConfigDTO;
import com.todaktodot.TDTD.admin.prompt.dto.AiPromptDTO;

import java.util.List;
import java.util.Optional;

public interface AdminFeedbackService {

    Optional<FeedbackConfigDTO> getCurrentConfig();

    FeedbackConfigDTO saveConfig(FeedbackConfigDTO.SaveRequest request);

    List<AiPromptDTO> getSelectableFeedbackPrompts();

    Optional<AiPromptDTO> getActivePrompt();
}
