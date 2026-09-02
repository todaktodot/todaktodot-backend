package com.todaktodot.TDTD.admin.vote.controller;


import com.todaktodot.TDTD.admin.vote.dto.AdminReleaseRequestDTO;
import com.todaktodot.TDTD.admin.vote.dto.AdminReportListDTO;
import com.todaktodot.TDTD.admin.vote.dto.AdminSuspendRequestDTO;
import com.todaktodot.TDTD.admin.vote.service.AdminReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;


@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/reports")
@Slf4j
public class AdminReportController {
    private static final int DEFAULT_PAGE_SIZE = 15;

    private final AdminReportService adminReportService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false, defaultValue = "REPORTED_COUNT") String sortBy,
                       @RequestParam(required = false, defaultValue = "") String keyword,
                       Model model) {
        String resolvedStatus = (status == null || status.isBlank()) ? "" : status;
        String resolvedSortBy = (sortBy == null || sortBy.isBlank()) ? "" : sortBy;

        //신고 리스트
        Page<AdminReportListDTO> reports = adminReportService.getReports(resolvedStatus, resolvedSortBy, keyword,
                PageRequest.of(page, DEFAULT_PAGE_SIZE)
        );

        model.addAttribute("reports", reports);
        model.addAttribute("status", resolvedStatus);
        model.addAttribute("sortBy", resolvedSortBy);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentDt", LocalDateTime.now());
        model.addAttribute("totalReportUserCount", adminReportService.getTotalReportUserCount());
        model.addAttribute("normalCount", adminReportService.getNormalUserCount());
        model.addAttribute("suspendCount", adminReportService.getSuspendedUserCount());
        model.addAttribute("weeklySuspendCount", adminReportService.getWeeklySuspendedUserCount());
        model.addAttribute("activeMenu", "report");

        return "admin/vote/report/list";
    }

    @GetMapping("/{userId}")
    public String detail(@PathVariable(name = "userId") Long userId, Model model) {
        model.addAttribute("report", adminReportService.getDetail(userId));

        return "admin/vote/report/detail";
    }

    @PostMapping("/suspend")
    @ResponseBody
    public Map<String, String> suspend(@RequestBody AdminSuspendRequestDTO request, Authentication authentication) {

        log.info("[Admin] 유저 정지 요청: userId={}, actor={}", request.getUserId(), authentication.getName());
        adminReportService.suspend(request);

        return Map.of("message", "정지 처리되었습니다.");
    }

    @PostMapping("/release")
    @ResponseBody
    public Map<String, String> release(@RequestBody AdminReleaseRequestDTO request, Authentication authentication) {

        log.info("[Admin] 유저 해제 요청: userId={}, actor={}", request.getUserId(), authentication.getName());
        adminReportService.release(request);

        return Map.of("message", "해제 처리되었습니다.");
    }
}
