package com.todaktodot.TDTD.admin.feedback.controller;

import com.todaktodot.TDTD.admin.feedback.dto.FeedbackConfigDTO;
import com.todaktodot.TDTD.admin.feedback.service.AdminFeedbackService;
import com.todaktodot.TDTD.admin.prompt.dto.AiPromptDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/feedback")
public class AdminFeedbackController {

    private final AdminFeedbackService adminFeedbackService;

    private static final List<Map<String, String>> AI_MODELS = List.of(
            Map.of("id", "gpt-4o-mini", "name", "GPT-4o Mini", "desc", "빠르고 저렴함 (권장)"),
            Map.of("id", "gpt-4o", "name", "GPT-4o", "desc", "고품질, 범용 모델"),
            Map.of("id", "gpt-4.1", "name", "GPT-4.1", "desc", "코딩에 강함, 1M 컨텍스트"),
            Map.of("id", "gpt-4.1-mini", "name", "GPT-4.1 Mini", "desc", "GPT-4.1의 빠른 버전"),
            Map.of("id", "gpt-5.2", "name", "GPT-5.2", "desc", "최신 플래그십 (400K 컨텍스트)")
    );

    @GetMapping
    public String settings(Model model) {
        var currentConfig = adminFeedbackService.getCurrentConfig().orElse(null);
        var activePrompt = adminFeedbackService.getActivePrompt().orElse(null);
        var selectablePrompts = adminFeedbackService.getSelectableFeedbackPrompts();

        model.addAttribute("config", currentConfig);
        model.addAttribute("activePrompt", activePrompt);
        model.addAttribute("prompts", selectablePrompts);
        model.addAttribute("aiModels", AI_MODELS);
        model.addAttribute("activeMenu", "feedback");

        return "admin/feedback/settings";
    }

    @PostMapping("/config")
    public String saveConfig(@ModelAttribute FeedbackConfigDTO.SaveRequest request,
                             RedirectAttributes redirectAttributes) {
        adminFeedbackService.saveConfig(request);
        redirectAttributes.addFlashAttribute("message", "피드백 설정이 저장되었습니다.");
        return "redirect:/admin/feedback";
    }
}
