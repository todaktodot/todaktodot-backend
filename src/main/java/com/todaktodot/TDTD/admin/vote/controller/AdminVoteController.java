package com.todaktodot.TDTD.admin.vote.controller;

import com.todaktodot.TDTD.admin.vote.dto.AdminVoteDetailDTO;
import com.todaktodot.TDTD.admin.vote.dto.AdminVoteListDTO;
import com.todaktodot.TDTD.admin.vote.dto.AdminVoteSearchCondition;
import com.todaktodot.TDTD.admin.vote.service.AdminVoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/vote")
@Slf4j
public class AdminVoteController {

    private final AdminVoteService adminVoteService;

    @GetMapping
    public String list(@ModelAttribute AdminVoteSearchCondition condition, Model model) {
        // 기간 미지정 시(최초 진입) 기본값으로 최근 1개월 노출
        if (condition.getStartDt() == null && condition.getEndDt() == null) {
            condition.setEndDt(LocalDate.now());
            condition.setStartDt(LocalDate.now().minusMonths(1));
        }

        Page<AdminVoteListDTO> votes = adminVoteService.getList(
                condition, PageRequest.of(condition.getPage(), condition.getSize()));

        model.addAttribute("votes", votes);
        model.addAttribute("condition", condition);
        model.addAttribute("stats", adminVoteService.getStats());
        model.addAttribute("activeMenu", "vote");

        return "admin/vote/list";
    }

    @GetMapping("/{voteId}")
    public String detail(@PathVariable Long voteId, Model model) {
        model.addAttribute("vote", adminVoteService.getDetail(voteId));

        return "admin/vote/detail";
    }

    @PostMapping("/{voteId}/hide")
    @ResponseBody
    public Map<String, String> hide(@PathVariable Long voteId, Authentication authentication) {
        log.info("[Admin] 투표 숨김 요청: voteId={}, actor={}", voteId, authentication.getName());
        adminVoteService.hide(voteId, authentication.getName());
        return Map.of("message", "숨김 처리되었습니다.");
    }

    @PostMapping("/{voteId}/restore")
    @ResponseBody
    public Map<String, String> restore(@PathVariable Long voteId, Authentication authentication) {
        log.info("[Admin] 투표 복구 요청: voteId={}, actor={}", voteId, authentication.getName());
        adminVoteService.restore(voteId, authentication.getName());
        return Map.of("message", "복구되었습니다.");
    }

    @DeleteMapping("/{voteId}")
    @ResponseBody
    public Map<String, String> delete(@PathVariable Long voteId, Authentication authentication) {
        log.info("[Admin] 투표 삭제 요청: voteId={}, actor={}", voteId, authentication.getName());
        adminVoteService.delete(voteId, authentication.getName());
        return Map.of("message", "삭제되었습니다.");
    }
}
