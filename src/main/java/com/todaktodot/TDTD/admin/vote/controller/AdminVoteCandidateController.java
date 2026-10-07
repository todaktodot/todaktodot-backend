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
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/vote-candidate")
@Slf4j
public class AdminVoteCandidateController {

    private final VoteCandidateService voteCandidateService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("candidates", voteCandidateService.getPendingList());
        model.addAttribute("activeMenu", "vote-candidate");

        return "admin/votecandidate/list";
    }

    @PostMapping("/generate")
    @ResponseBody
    public Map<String, String> generate(Authentication authentication) {
        log.info("[Admin] 투표 후보 수동 생성 요청: actor={}", authentication.getName());
        int count = voteCandidateService.generate().size();
        return Map.of("message", count + "개의 후보를 만들었습니다.");
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
