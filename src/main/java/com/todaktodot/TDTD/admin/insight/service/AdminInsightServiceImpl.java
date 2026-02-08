package com.todaktodot.TDTD.admin.insight.service;

import com.todaktodot.TDTD.admin.insight.dto.InsightConfigDTO;
import com.todaktodot.TDTD.admin.prompt.dto.AiPromptDTO;
import com.todaktodot.TDTD.admin.prompt.repository.AiPromptRepository;
import com.todaktodot.TDTD.admin.prompt.repository.entity.PromptType;
import com.todaktodot.TDTD.domain.insight.repository.InsightConfigRepository;
import com.todaktodot.TDTD.domain.insight.repository.entity.InsightConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminInsightServiceImpl implements AdminInsightService {

    private final InsightConfigRepository insightConfigRepository;
    private final AiPromptRepository aiPromptRepository;

    private static final Long ADMIN_USER_ID = 0L;

    /**
     * 현재 설정정보 조회
     */
    @Override
    public Optional<InsightConfigDTO> getCurrentConfig() {
        return insightConfigRepository.findTopByDelYnOrderByConfigIdDesc("N")
                .map(InsightConfigDTO::from);
    }

    /**
     * 새로운 설정 정보 저장
     */
    @Override
    @Transactional
    public InsightConfigDTO saveConfig(InsightConfigDTO.SaveRequest request) {
        // 기존 활성 설정 소프트삭제
        insightConfigRepository.findTopByDelYnOrderByConfigIdDesc("N")
                .ifPresent(existing -> existing.softDelete(ADMIN_USER_ID));

        // 새 설정 행 저장
        InsightConfig newConfig = InsightConfig.builder()
                .promptId(request.getPromptId())
                .aiModel(request.getAiModel())
                .temperature(request.getTemperature())
                .regrId(ADMIN_USER_ID)
                .updrId(ADMIN_USER_ID)
                .build();

        InsightConfig saved = insightConfigRepository.save(newConfig);
        return InsightConfigDTO.from(saved);
    }

    /**
     * 적용할 수 있는 프롬프트 목록 조회
     */
    @Override
    public List<AiPromptDTO> getSelectableInsightPrompts() {
        return aiPromptRepository.findLatestActivePerGroupByType(PromptType.REPORT_INSIGHT).stream()
                .map(AiPromptDTO::from)
                .collect(Collectors.toList());
    }

    /**
     * 현재 활성중인 프롬프트 조회
     */
    @Override
    public Optional<AiPromptDTO> getActivePrompt() {
        return getCurrentConfig()
                .filter(config -> config.getPromptId() != null)
                .flatMap(config -> aiPromptRepository.findById(config.getPromptId()))
                .map(AiPromptDTO::from);
    }
}
