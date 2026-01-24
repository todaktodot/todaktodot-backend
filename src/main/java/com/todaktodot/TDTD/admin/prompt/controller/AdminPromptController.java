package com.todaktodot.TDTD.admin.prompt.controller;

import com.todaktodot.TDTD.admin.prompt.dto.AiPromptDTO;
import com.todaktodot.TDTD.admin.prompt.dto.SituationCategoryDTO;
import com.todaktodot.TDTD.admin.prompt.service.AdminPromptService;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardSubject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/prompt")
public class AdminPromptController {

    private final AdminPromptService adminPromptService;

    @GetMapping
    public String list(Model model) {
        List<AiPromptDTO> prompts = adminPromptService.getAllPrompts();
        Map<CardSubject, List<SituationCategoryDTO>> categoriesBySubject =
                adminPromptService.getCategoriesGroupedBySubject();

        model.addAttribute("prompts", prompts);
        model.addAttribute("categoriesBySubject", categoriesBySubject);
        model.addAttribute("subjects", CardSubject.values());
        model.addAttribute("promptCount", adminPromptService.getPromptCount());
        model.addAttribute("categoryCount", adminPromptService.getCategoryCount());
        model.addAttribute("activeMenu", "prompt");

        return "admin/prompt/list";
    }

    // ==================== 프롬프트 관리 ====================

    @GetMapping("/prompt/{promptId}")
    public String promptDetail(@PathVariable Long promptId, Model model) {
        AiPromptDTO prompt = adminPromptService.getPrompt(promptId);

        model.addAttribute("prompt", prompt);
        model.addAttribute("activeMenu", "prompt");

        return "admin/prompt/prompt-detail";
    }

    @GetMapping("/prompt/new")
    public String promptNewForm(Model model) {
        model.addAttribute("activeMenu", "prompt");
        return "admin/prompt/prompt-form";
    }

    @PostMapping("/prompt/new")
    public String createPrompt(@ModelAttribute AiPromptDTO.CreateRequest request,
                               RedirectAttributes redirectAttributes) {
        adminPromptService.createPrompt(request);
        redirectAttributes.addFlashAttribute("message", "프롬프트가 생성되었습니다.");
        return "redirect:/admin/prompt";
    }

    @GetMapping("/prompt/{promptId}/edit")
    public String promptEditForm(@PathVariable Long promptId, Model model) {
        AiPromptDTO prompt = adminPromptService.getPrompt(promptId);

        model.addAttribute("prompt", prompt);
        model.addAttribute("activeMenu", "prompt");

        return "admin/prompt/prompt-edit";
    }

    @PostMapping("/prompt/{promptId}/edit")
    public String updatePrompt(@PathVariable Long promptId,
                               @ModelAttribute AiPromptDTO.UpdateRequest request,
                               RedirectAttributes redirectAttributes) {
        adminPromptService.updatePrompt(promptId, request);
        redirectAttributes.addFlashAttribute("message", "프롬프트가 수정되었습니다.");
        return "redirect:/admin/prompt/prompt/" + promptId;
    }

    @PostMapping("/prompt/{promptId}/toggle-status")
    public String togglePromptStatus(@PathVariable Long promptId,
                                     RedirectAttributes redirectAttributes) {
        adminPromptService.togglePromptStatus(promptId);
        redirectAttributes.addFlashAttribute("message", "프롬프트 상태가 변경되었습니다.");
        return "redirect:/admin/prompt";
    }

    @PostMapping("/prompt/{promptId}/delete")
    public String deletePrompt(@PathVariable Long promptId,
                               RedirectAttributes redirectAttributes) {
        adminPromptService.deletePrompt(promptId);
        redirectAttributes.addFlashAttribute("message", "프롬프트가 삭제되었습니다.");
        return "redirect:/admin/prompt";
    }

    // ==================== 예시 상황 카테고리 관리 ====================

    @GetMapping("/category/new")
    public String categoryNewForm(Model model) {
        model.addAttribute("subjects", CardSubject.values());
        model.addAttribute("activeMenu", "prompt");
        return "admin/prompt/category-form";
    }

    @PostMapping("/category/new")
    public String createCategory(@ModelAttribute SituationCategoryDTO.CreateRequest request,
                                 RedirectAttributes redirectAttributes) {
        adminPromptService.createCategory(request);
        redirectAttributes.addFlashAttribute("message", "카테고리가 생성되었습니다.");
        return "redirect:/admin/prompt";
    }

    @GetMapping("/category/{categoryId}/edit")
    public String categoryEditForm(@PathVariable Long categoryId, Model model) {
        SituationCategoryDTO category = adminPromptService.getCategory(categoryId);

        model.addAttribute("category", category);
        model.addAttribute("subjects", CardSubject.values());
        model.addAttribute("activeMenu", "prompt");

        return "admin/prompt/category-edit";
    }

    @PostMapping("/category/{categoryId}/edit")
    public String updateCategory(@PathVariable Long categoryId,
                                 @ModelAttribute SituationCategoryDTO.UpdateRequest request,
                                 RedirectAttributes redirectAttributes) {
        adminPromptService.updateCategory(categoryId, request);
        redirectAttributes.addFlashAttribute("message", "카테고리가 수정되었습니다.");
        return "redirect:/admin/prompt";
    }

    @PostMapping("/category/{categoryId}/toggle-status")
    public String toggleCategoryStatus(@PathVariable Long categoryId,
                                       RedirectAttributes redirectAttributes) {
        adminPromptService.toggleCategoryStatus(categoryId);
        redirectAttributes.addFlashAttribute("message", "카테고리 상태가 변경되었습니다.");
        return "redirect:/admin/prompt";
    }

    @PostMapping("/category/{categoryId}/delete")
    public String deleteCategory(@PathVariable Long categoryId,
                                 RedirectAttributes redirectAttributes) {
        adminPromptService.deleteCategory(categoryId);
        redirectAttributes.addFlashAttribute("message", "카테고리가 삭제되었습니다.");
        return "redirect:/admin/prompt";
    }
}
