package com.todaktodot.TDTD.admin.prompt.service;

import com.todaktodot.TDTD.admin.prompt.dto.AiPromptDTO;
import com.todaktodot.TDTD.admin.prompt.dto.SituationCategoryDTO;
import com.todaktodot.TDTD.admin.prompt.repository.AiPromptRepository;
import com.todaktodot.TDTD.admin.prompt.repository.SituationCategoryRepository;
import com.todaktodot.TDTD.admin.prompt.repository.entity.AiPromptEntity;
import com.todaktodot.TDTD.admin.prompt.repository.entity.SituationCategoryEntity;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardSubject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPromptServiceImpl implements AdminPromptService {

    private final AiPromptRepository aiPromptRepository;
    private final SituationCategoryRepository situationCategoryRepository;

    private static final Long ADMIN_USER_ID = 0L;

    // ==================== AI 프롬프트 관련 ====================

    @Override
    public List<AiPromptDTO> getAllPrompts() {
        return aiPromptRepository.findAllActive().stream()
                .map(AiPromptDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    public AiPromptDTO getPrompt(Long promptId) {
        AiPromptEntity entity = aiPromptRepository.findById(promptId)
                .orElseThrow(() -> new IllegalArgumentException("프롬프트를 찾을 수 없습니다: " + promptId));
        return AiPromptDTO.from(entity);
    }

    @Override
    public AiPromptDTO getActivePromptByName(String promptName) {
        AiPromptEntity entity = aiPromptRepository
                .findTopByPromptNameAndUseYnAndDelYnOrderByVersionDesc(promptName, "Y", "N")
                .orElseThrow(() -> new IllegalArgumentException("활성화된 프롬프트가 없습니다: " + promptName));
        return AiPromptDTO.from(entity);
    }

    @Override
    @Transactional
    public AiPromptDTO createPrompt(AiPromptDTO.CreateRequest request) {
        Integer maxVersion = aiPromptRepository.findMaxVersionByPromptName(request.getPromptName());
        int newVersion = maxVersion != null ? maxVersion + 1 : 1;

        AiPromptEntity entity = AiPromptEntity.builder()
                .promptName(request.getPromptName())
                .promptDesc(request.getPromptDesc())
                .promptContent(request.getPromptContent())
                .version(newVersion)
                .regrId(ADMIN_USER_ID)
                .updrId(ADMIN_USER_ID)
                .build();

        AiPromptEntity saved = aiPromptRepository.save(entity);
        return AiPromptDTO.from(saved);
    }

    @Override
    @Transactional
    public void updatePrompt(Long promptId, AiPromptDTO.UpdateRequest request) {
        AiPromptEntity entity = aiPromptRepository.findById(promptId)
                .orElseThrow(() -> new IllegalArgumentException("프롬프트를 찾을 수 없습니다: " + promptId));

        entity.update(request.getPromptDesc(), request.getPromptContent(), ADMIN_USER_ID);
    }

    @Override
    @Transactional
    public void togglePromptStatus(Long promptId) {
        AiPromptEntity entity = aiPromptRepository.findById(promptId)
                .orElseThrow(() -> new IllegalArgumentException("프롬프트를 찾을 수 없습니다: " + promptId));

        String newUseYn = entity.isActive() ? "N" : "Y";
        entity.updateUseYn(newUseYn, ADMIN_USER_ID);
    }

    @Override
    @Transactional
    public void deletePrompt(Long promptId) {
        AiPromptEntity entity = aiPromptRepository.findById(promptId)
                .orElseThrow(() -> new IllegalArgumentException("프롬프트를 찾을 수 없습니다: " + promptId));

        aiPromptRepository.delete(entity);
    }

    // ==================== 예시 상황 카테고리 관련 ====================

    @Override
    public List<SituationCategoryDTO> getAllCategories() {
        return situationCategoryRepository.findAllActive().stream()
                .map(SituationCategoryDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    public Map<CardSubject, List<SituationCategoryDTO>> getCategoriesGroupedBySubject() {
        List<SituationCategoryDTO> allCategories = getAllCategories();

        Map<CardSubject, List<SituationCategoryDTO>> grouped = new EnumMap<>(CardSubject.class);
        for (CardSubject subject : CardSubject.values()) {
            grouped.put(subject, allCategories.stream()
                    .filter(c -> c.getSubject() == subject)
                    .collect(Collectors.toList()));
        }
        return grouped;
    }

    @Override
    public List<SituationCategoryDTO> getCategoriesBySubject(CardSubject subject) {
        return situationCategoryRepository.findBySubject(subject).stream()
                .map(SituationCategoryDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    public SituationCategoryDTO getCategory(Long categoryId) {
        SituationCategoryEntity entity = situationCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: " + categoryId));
        return SituationCategoryDTO.from(entity);
    }

    @Override
    @Transactional
    public SituationCategoryDTO createCategory(SituationCategoryDTO.CreateRequest request) {
        SituationCategoryEntity entity = SituationCategoryEntity.builder()
                .subject(request.getSubject())
                .categoryName(request.getCategoryName())
                .categoryDesc(request.getCategoryDesc())
                .sortOrder(request.getSortOrder())
                .regrId(ADMIN_USER_ID)
                .updrId(ADMIN_USER_ID)
                .build();

        SituationCategoryEntity saved = situationCategoryRepository.save(entity);
        return SituationCategoryDTO.from(saved);
    }

    @Override
    @Transactional
    public void updateCategory(Long categoryId, SituationCategoryDTO.UpdateRequest request) {
        SituationCategoryEntity entity = situationCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: " + categoryId));

        entity.update(request.getCategoryName(), request.getCategoryDesc(),
                request.getSortOrder(), ADMIN_USER_ID);
    }

    @Override
    @Transactional
    public void toggleCategoryStatus(Long categoryId) {
        SituationCategoryEntity entity = situationCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: " + categoryId));

        String newUseYn = entity.isActive() ? "N" : "Y";
        entity.updateUseYn(newUseYn, ADMIN_USER_ID);
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        SituationCategoryEntity entity = situationCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: " + categoryId));

        situationCategoryRepository.delete(entity);
    }

    // ==================== 통계 ====================

    @Override
    public long getPromptCount() {
        return aiPromptRepository.countByDelYn("N");
    }

    @Override
    public long getCategoryCount() {
        return situationCategoryRepository.countByDelYn("N");
    }

    @Override
    public Map<CardSubject, Long> getCategoryCountBySubject() {
        Map<CardSubject, Long> counts = new EnumMap<>(CardSubject.class);
        for (CardSubject subject : CardSubject.values()) {
            counts.put(subject, situationCategoryRepository.countBySubjectAndDelYn(subject, "N"));
        }
        return counts;
    }
}
