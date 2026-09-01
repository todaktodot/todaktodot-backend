package com.todaktodot.TDTD.admin.vote.controller;

import com.todaktodot.TDTD.admin.couple.dto.CoupleListDTO;
import com.todaktodot.TDTD.admin.vote.dto.AdminReportListDTO;
import com.todaktodot.TDTD.admin.vote.service.AdminReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

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
}
