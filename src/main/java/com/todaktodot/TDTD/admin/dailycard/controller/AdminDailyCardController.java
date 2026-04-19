package com.todaktodot.TDTD.admin.dailycard.controller;

import com.todaktodot.TDTD.admin.dailycard.dto.CardStatisticsDTO;
import com.todaktodot.TDTD.admin.dailycard.dto.DailyCardDetailDTO;
import com.todaktodot.TDTD.admin.dailycard.dto.DailyCardListDTO;
import com.todaktodot.TDTD.admin.dailycard.dto.DailyCardSearchDTO;
import com.todaktodot.TDTD.admin.dailycard.dto.DailyCardUpdateDTO;
import com.todaktodot.TDTD.admin.dailycard.service.AdminDailyCardService;
import com.todaktodot.TDTD.admin.prompt.dto.SituationCategoryDTO;
import com.todaktodot.TDTD.admin.prompt.service.AdminPromptService;
import com.todaktodot.TDTD.domain.dailycard.dto.request.GenerateDailyCardRequestDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.response.GenerateDailyCardResponseDTO;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardMode;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardSubject;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardType;
import com.todaktodot.TDTD.domain.dailycard.service.DailyCardService;
import com.todaktodot.TDTD.domain.dailycard.service.DailyCardServiceImpl;

import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/daily-card")
public class AdminDailyCardController {

    private final AdminDailyCardService adminDailyCardService;
    private final AdminPromptService adminPromptService;
    private final DailyCardService dailyCardService;

    @GetMapping
    public String list(@ModelAttribute DailyCardSearchDTO searchDTO, Model model) {
        Page<DailyCardListDTO> cards = adminDailyCardService.searchDailyCards(searchDTO);

        // 통계 정보
        CardStatisticsDTO stats = adminDailyCardService.getCardStatistics();

        model.addAttribute("cards", cards);
        model.addAttribute("searchDTO", searchDTO);
        model.addAttribute("modes", CardMode.values());
        model.addAttribute("subjects", CardSubject.values());
        model.addAttribute("types", CardType.values());
        model.addAttribute("totalCount", stats.getTotalCount());
        model.addAttribute("activeCount", stats.getActiveCount());
        model.addAttribute("inactiveCount", stats.getInactiveCount());
        model.addAttribute("activeMenu", "dailycard");

        return "admin/dailycard/list";
    }

    @GetMapping("/{cardId}")
    public String detail(@PathVariable Long cardId, Model model) {
        DailyCardDetailDTO card = adminDailyCardService.getDailyCardDetail(cardId);

        // AI 생성 정보 조회 (AI로 생성된 카드인 경우에만 존재)
        var generationInfo = adminDailyCardService.getAiGenerationInfo(cardId);

        model.addAttribute("card", card);
        model.addAttribute("generationInfo", generationInfo.orElse(null));
        model.addAttribute("modes", CardMode.values());
        model.addAttribute("subjects", CardSubject.values());
        model.addAttribute("types", CardType.values());
        model.addAttribute("activeMenu", "dailycard");

        return "admin/dailycard/detail";
    }

    @GetMapping("/{cardId}/edit")
    public String editForm(@PathVariable Long cardId, Model model) {
        DailyCardDetailDTO card = adminDailyCardService.getDailyCardDetail(cardId);

        populateEditModel(model, card, card.getSituation());

        return "admin/dailycard/edit";
    }

    @PostMapping("/{cardId}/edit")
    public String update(@PathVariable Long cardId,
                         @Valid @ModelAttribute DailyCardUpdateDTO updateDTO,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {

        if (bindingResult.hasErrors()) {
            DailyCardDetailDTO card = adminDailyCardService.getDailyCardDetail(cardId);
            String selectedSituation = updateDTO.getSituation() != null ? updateDTO.getSituation() : card.getSituation();
            populateEditModel(model, card, selectedSituation);
            return "admin/dailycard/edit";
        }

        DailyCardDetailDTO card = adminDailyCardService.getDailyCardDetail(cardId);
        if (!isSituationSelectionAllowed(card.getSubject(), card.getSituation(), updateDTO.getSituation())) {
            populateEditModel(model, card, card.getSituation());
            model.addAttribute("situationError", "현재 카드 주제에서 선택할 수 없는 상황입니다.");
            return "admin/dailycard/edit";
        }

        updateDTO.setCardId(cardId);
        Long newCardId = adminDailyCardService.updateDailyCard(updateDTO);

        redirectAttributes.addFlashAttribute("message", "카드가 수정되었습니다.");
        return "redirect:/admin/daily-card/" + newCardId;
    }

    @PostMapping("/{cardId}/delete")
    public String delete(@PathVariable Long cardId, RedirectAttributes redirectAttributes) {
        adminDailyCardService.deleteDailyCard(cardId);

        redirectAttributes.addFlashAttribute("message", "카드가 삭제되었습니다.");
        return "redirect:/admin/daily-card";
    }

    @PostMapping("/{cardId}/toggle-status")
    public String toggleStatus(@PathVariable Long cardId, RedirectAttributes redirectAttributes) {
        adminDailyCardService.toggleUseYn(cardId);

        redirectAttributes.addFlashAttribute("message", "상태가 변경되었습니다.");
        return "redirect:/admin/daily-card";
    }

    // ==================== 데일리카드 AI 생성 ====================

    private static final List<Map<String, String>> AI_MODELS = List.of(
            Map.of("id", "gpt-4o-mini", "name", "GPT-4o Mini", "desc", "빠르고 저렴함 (권장)"),
            Map.of("id", "gpt-4o", "name", "GPT-4o", "desc", "고품질, 범용 모델"),
            Map.of("id", "gpt-4.1", "name", "GPT-4.1", "desc", "코딩에 강함, 1M 컨텍스트"),
            Map.of("id", "gpt-4.1-mini", "name", "GPT-4.1 Mini", "desc", "GPT-4.1의 빠른 버전"),
            Map.of("id", "gpt-5.2", "name", "GPT-5.2", "desc", "플래그십 (400K 컨텍스트)"),
            Map.of("id", "gpt-5.4", "name", "GPT-5.4", "desc", "최신 플래그십 (1M 컨텍스트)")
    );

    @GetMapping("/generate")
    public String generateForm(Model model) {
        Map<CardSubject, List<SituationCategoryDTO>> categoriesBySubject =
                adminPromptService.getCategoriesGroupedBySubject();

        var prompts = adminPromptService.getActivePromptsByType("CARD_GENERATION");

        // 가장 최근 프롬프트(ID가 가장 큰)를 기본값으로 설정
        Long defaultPromptId = prompts.stream()
                .mapToLong(p -> p.getPromptId())
                .max()
                .orElse(0L);

        model.addAttribute("modes", CardMode.values());
        model.addAttribute("subjects", CardSubject.values());
        model.addAttribute("types", CardType.values());
        model.addAttribute("categoriesBySubject", categoriesBySubject);
        model.addAttribute("prompts", prompts);
        model.addAttribute("defaultPromptId", defaultPromptId);
        model.addAttribute("aiModels", AI_MODELS);
        model.addAttribute("activeMenu", "generate");

        return "admin/dailycard/generate";
    }

    @PostMapping("/generate")
    public String generate(@ModelAttribute GenerateDailyCardRequestDTO requestDTO,
                           RedirectAttributes redirectAttributes) {
        try {
            GenerateDailyCardResponseDTO response = dailyCardService.generateDailyCard(requestDTO);
            redirectAttributes.addFlashAttribute("message",
                    "카드가 생성되었습니다! (ID: " + response.getCardId() + ")");
            return "redirect:/admin/daily-card/" + response.getCardId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "카드 생성에 실패했습니다: " + e.getMessage());
            return "redirect:/admin/daily-card/generate";
        }
    }

    /**
     * AI 프롬프트 미리보기 API
     * 생성 버튼을 누르기 전에 최종 프롬프트를 확인할 수 있도록 제공
     * 영역별로 분리해서 반환 (prefix, admin, suffix)
     */
    @GetMapping("/generate/preview-prompt")
    @ResponseBody
    public ResponseEntity<Map<String, String>> previewPrompt(
            @RequestParam CardMode mode,
            @RequestParam CardSubject subject,
            @RequestParam CardType type,
            @RequestParam(required = false) String situationCategory,
            @RequestParam(required = false) Long promptId) {

        Map<String, String> separated = ((DailyCardServiceImpl) dailyCardService)
                .previewPromptSeparated(mode, subject, type, situationCategory, promptId);

        return ResponseEntity.ok(separated);
    }

    private void populateEditModel(Model model, DailyCardDetailDTO card, String selectedSituation) {
        model.addAttribute("card", card);
        model.addAttribute("modes", CardMode.values());
        model.addAttribute("subjects", CardSubject.values());
        model.addAttribute("types", CardType.values());
        model.addAttribute("activeMenu", "dailycard");

        addSituationSelectionModel(model, card.getSubject(), selectedSituation);
    }

    private void addSituationSelectionModel(Model model, CardSubject subject, String selectedSituation) {
        Map<CardSubject, List<SituationCategoryDTO>> categoriesBySubject =
                adminPromptService.getCategoriesGroupedBySubject();
        List<SituationCategoryDTO> allSituationOptions = subject != null
                ? categoriesBySubject.getOrDefault(subject, List.of())
                : List.of();
        List<SituationCategoryDTO> situationOptions = allSituationOptions.stream()
                .filter(SituationCategoryDTO::isActive)
                .toList();

        model.addAttribute("situationOptions", situationOptions);
        model.addAttribute("selectedSituation", selectedSituation);

        if (selectedSituation == null || selectedSituation.isBlank() || subject == null) {
            model.addAttribute("selectedSituationCompatibilityState", null);
            return;
        }

        boolean selectedSituationInActiveOptions = situationOptions.stream()
                .anyMatch(category -> selectedSituation.equals(category.getCategoryName()));

        if (selectedSituationInActiveOptions) {
            model.addAttribute("selectedSituationCompatibilityState", null);
            return;
        }

        boolean selectedSituationExistsForSubject = allSituationOptions.stream()
                .anyMatch(category -> selectedSituation.equals(category.getCategoryName()));

        model.addAttribute(
                "selectedSituationCompatibilityState",
                selectedSituationExistsForSubject ? "inactive" : "missing"
        );
    }

    private boolean isSituationSelectionAllowed(CardSubject subject, String currentSituation, String submittedSituation) {
        if (submittedSituation == null || submittedSituation.isBlank()) {
            return true;
        }

        if (submittedSituation.equals(currentSituation)) {
            return true;
        }

        if (subject == null) {
            return false;
        }

        Map<CardSubject, List<SituationCategoryDTO>> categoriesBySubject =
                adminPromptService.getCategoriesGroupedBySubject();

        return categoriesBySubject.getOrDefault(subject, List.of()).stream()
                .filter(SituationCategoryDTO::isActive)
                .anyMatch(category -> submittedSituation.equals(category.getCategoryName()));
    }
}
