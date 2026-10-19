package com.todaktodot.TDTD.admin.vote.controller;

import com.todaktodot.TDTD.domain.vote.dto.request.VoteCandidateApproveRequestDTO;
import com.todaktodot.TDTD.domain.vote.dto.response.VoteCandidateApproveResultDTO;
import com.todaktodot.TDTD.domain.vote.service.VoteCandidateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/vote-candidate")
@Slf4j
public class AdminVoteCandidateController {

    private final VoteCandidateService voteCandidateService;

    //AdminInsightController 등 기존 어드민 화면과 같은 목록
    private static final List<Map<String, String>> AI_MODELS = List.of(
            Map.of("id", "gpt-4o-mini", "name", "GPT-4o Mini", "desc", "빠르고 저렴함"),
            Map.of("id", "gpt-4o", "name", "GPT-4o", "desc", "고품질, 범용 모델"),
            Map.of("id", "gpt-4.1", "name", "GPT-4.1", "desc", "코딩에 강함, 1M 컨텍스트"),
            Map.of("id", "gpt-4.1-mini", "name", "GPT-4.1 Mini", "desc", "GPT-4.1의 빠른 버전"),
            Map.of("id", "gpt-5.2", "name", "GPT-5.2", "desc", "플래그십 (400K 컨텍스트)"),
            Map.of("id", "gpt-5.4", "name", "GPT-5.4", "desc", "최신 플래그십 (1M 컨텍스트)")
    );

    @GetMapping
    public String list(Model model) {
        model.addAttribute("candidates", voteCandidateService.getPendingList());
        model.addAttribute("aiModels", AI_MODELS);
        model.addAttribute("defaultAiModel", voteCandidateService.getDefaultAiModel());
        model.addAttribute("activeMenu", "vote-candidate");

        return "admin/votecandidate/list";
    }

    @PostMapping("/generate")
    @ResponseBody
    public Map<String, String> generate(@RequestParam(required = false) String aiModel,
                                        Authentication authentication) {
        //화면 밖에서 온 모델명은 쓰지 않는다. 목록에 없는 값이면 설정값으로 생성
        String selected = isKnownModel(aiModel) ? aiModel : null;

        log.info("[Admin] 투표 후보 수동 생성 요청: actor={}, model={}", authentication.getName(), selected);
        int count = voteCandidateService.generate(selected).size();
        return Map.of("message", count + "개의 후보를 만들었습니다.");
    }

    private boolean isKnownModel(String aiModel) {
        return aiModel != null && AI_MODELS.stream().anyMatch(model -> model.get("id").equals(aiModel));
    }

    @PostMapping("/approve")
    @ResponseBody
    public Map<String, Object> approve(@RequestBody List<VoteCandidateApproveRequestDTO> requests,
                                       Authentication authentication) {
        VoteCandidateApproveResultDTO result = voteCandidateService.approve(requests, authentication.getName());

        //일부만 실패해도 성공분은 이미 등록된 상태라 함께 반환
        return Map.of(
                "message", result.getApprovedCount() + "개의 투표를 등록했습니다.",
                "approvedCount", result.getApprovedCount(),
                "failures", result.getFailures()
        );
    }

    @PostMapping("/reject")
    @ResponseBody
    public Map<String, String> reject(@RequestBody List<Long> candidateIds, Authentication authentication) {
        int count = voteCandidateService.reject(candidateIds, authentication.getName());
        return Map.of("message", count + "개의 후보를 반려했습니다.");
    }
}
