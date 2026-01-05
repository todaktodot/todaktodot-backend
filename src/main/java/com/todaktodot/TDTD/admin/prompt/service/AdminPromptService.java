package com.todaktodot.TDTD.admin.prompt.service;

import com.todaktodot.TDTD.admin.prompt.dto.AiPromptDTO;
import com.todaktodot.TDTD.admin.prompt.dto.SituationCategoryDTO;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardSubject;

import java.util.List;
import java.util.Map;

public interface AdminPromptService {

    // AI 프롬프트 관련
    List<AiPromptDTO> getAllPrompts();

    AiPromptDTO getPrompt(Long promptId);

    AiPromptDTO getActivePromptByName(String promptName);

    AiPromptDTO createPrompt(AiPromptDTO.CreateRequest request);

    void updatePrompt(Long promptId, AiPromptDTO.UpdateRequest request);

    void togglePromptStatus(Long promptId);

    void deletePrompt(Long promptId);

    // 예시 상황 카테고리 관련
    List<SituationCategoryDTO> getAllCategories();

    Map<CardSubject, List<SituationCategoryDTO>> getCategoriesGroupedBySubject();

    List<SituationCategoryDTO> getCategoriesBySubject(CardSubject subject);

    SituationCategoryDTO getCategory(Long categoryId);

    SituationCategoryDTO createCategory(SituationCategoryDTO.CreateRequest request);

    void updateCategory(Long categoryId, SituationCategoryDTO.UpdateRequest request);

    void toggleCategoryStatus(Long categoryId);

    void deleteCategory(Long categoryId);

    // 통계
    long getPromptCount();

    long getCategoryCount();

    Map<CardSubject, Long> getCategoryCountBySubject();
}
