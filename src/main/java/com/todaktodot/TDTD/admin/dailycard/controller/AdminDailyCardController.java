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

        model.addAttribute("card", card);
        model.addAttribute("modes", CardMode.values());
        model.addAttribute("subjects", CardSubject.values());
        model.addAttribute("types", CardType.values());
        model.addAttribute("activeMenu", "dailycard");

        return "admin/dailycard/detail";
    }

    @GetMapping("/{cardId}/edit")
    public String editForm(@PathVariable Long cardId, Model model) {
        DailyCardDetailDTO card = adminDailyCardService.getDailyCardDetail(cardId);

        model.addAttribute("card", card);
        model.addAttribute("modes", CardMode.values());
        model.addAttribute("subjects", CardSubject.values());
        model.addAttribute("types", CardType.values());
        model.addAttribute("activeMenu", "dailycard");

        return "admin/dailycard/edit";
    }

    @PostMapping("/{cardId}/edit")
    public String update(@PathVariable Long cardId,
                         @Valid @ModelAttribute DailyCardUpdateDTO updateDTO,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("modes", CardMode.values());
            model.addAttribute("subjects", CardSubject.values());
            model.addAttribute("types", CardType.values());
            model.addAttribute("activeMenu", "dailycard");
            return "admin/dailycard/edit";
        }

        updateDTO.setCardId(cardId);
        adminDailyCardService.updateDailyCard(updateDTO);

        redirectAttributes.addFlashAttribute("message", "카드가 수정되었습니다.");
        return "redirect:/admin/daily-card/" + cardId;
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

    @GetMapping("/generate")
    public String generateForm(Model model) {
        Map<CardSubject, List<SituationCategoryDTO>> categoriesBySubject =
                adminPromptService.getCategoriesGroupedBySubject();

        var prompts = adminPromptService.getAllPrompts();

        // 가장 최근 프롬프트(ID가 가장 큰)를 기본값으로 설정
        Long defaultPromptId = prompts.stream()
                .filter(p -> p.isActive())
                .mapToLong(p -> p.getPromptId())
                .max()
                .orElse(0L);

        model.addAttribute("modes", CardMode.values());
        model.addAttribute("subjects", CardSubject.values());
        model.addAttribute("types", CardType.values());
        model.addAttribute("categoriesBySubject", categoriesBySubject);
        model.addAttribute("prompts", prompts);
        model.addAttribute("defaultPromptId", defaultPromptId);
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
}
