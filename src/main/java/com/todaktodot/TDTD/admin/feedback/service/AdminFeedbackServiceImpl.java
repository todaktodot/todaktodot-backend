package com.todaktodot.TDTD.admin.feedback.service;

import com.todaktodot.TDTD.admin.feedback.dto.FeedbackConfigDTO;
import com.todaktodot.TDTD.admin.insight.dto.InsightConfigDTO;
import com.todaktodot.TDTD.admin.prompt.dto.AiPromptDTO;
import com.todaktodot.TDTD.admin.prompt.repository.AiPromptRepository;
import com.todaktodot.TDTD.admin.prompt.repository.entity.AiPromptEntity;
import com.todaktodot.TDTD.admin.prompt.repository.entity.PromptType;
import com.todaktodot.TDTD.domain.feedback.repository.AiFeedbackConfigRepository;
import com.todaktodot.TDTD.domain.feedback.repository.entity.AiFeedbackConfigEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminFeedbackServiceImpl implements AdminFeedbackService {

    private final AiFeedbackConfigRepository feedbackConfigRepository;
    private final AiPromptRepository aiPromptRepository;

    private static final Long ADMIN_USER_ID = 0L;

    @Override
    public Optional<FeedbackConfigDTO> getCurrentConfig() {
        return feedbackConfigRepository.findTopByDelYnOrderByConfigIdDesc("N")
                .map(FeedbackConfigDTO::from);
    }

    @Override
    @Transactional
    public FeedbackConfigDTO saveConfig(FeedbackConfigDTO.SaveRequest request) {
        // 기존 활성 설정 소프트삭제
        feedbackConfigRepository.findTopByDelYnOrderByConfigIdDesc("N")
                .ifPresent(existing -> existing.softDelete(ADMIN_USER_ID));

        // 새 설정 행 INSERT
        AiFeedbackConfigEntity newConfig = AiFeedbackConfigEntity.builder()
                .promptId(request.getPromptId())
                .aiModel(request.getAiModel())
                .temperature(request.getTemperature())
                .regrId(ADMIN_USER_ID)
                .updrId(ADMIN_USER_ID)
                .build();

        AiFeedbackConfigEntity saved = feedbackConfigRepository.save(newConfig);
        return FeedbackConfigDTO.from(saved);
    }

    @Override
    public List<AiPromptDTO> getSelectableFeedbackPrompts() {
        return aiPromptRepository.findLatestActivePerGroupByType(PromptType.CARD_FEEDBACK).stream()
                .map(AiPromptDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<AiPromptDTO> getActivePrompt() {
        return getCurrentConfig()
                .filter(config -> config.getPromptId() != null)
                .flatMap(config -> aiPromptRepository.findById(config.getPromptId()))
                .map(AiPromptDTO::from);
    }

    @Override
    public List<FeedbackConfigDTO> getConfigHistory() {
        return getCurrentConfig()
                .map(FeedbackConfigDTO::getPromptId)
                .map(feedbackConfigRepository::findAllByPromptIdOrderByConfigIdDesc)
                .map(configHistory -> configHistory.stream()
                        .map(FeedbackConfigDTO::from)
                        .toList())
                .orElseGet(Collections::emptyList);
    }
}
