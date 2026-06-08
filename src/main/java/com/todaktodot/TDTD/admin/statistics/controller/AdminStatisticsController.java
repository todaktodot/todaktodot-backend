package com.todaktodot.TDTD.admin.statistics.controller;

import com.todaktodot.TDTD.admin.statistics.dto.StatisticsPageDTO;
import com.todaktodot.TDTD.admin.statistics.service.AdminStatisticsService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AdminStatisticsController {

    private final AdminStatisticsService adminStatisticsService;

    @GetMapping("/admin/statistics")
    public String index(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model
    ) {
        StatisticsPageDTO page = adminStatisticsService.getWeeklyStatistics(startDate, endDate);
        model.addAttribute("page", page);
        model.addAttribute("activeMenu", "statistics");
        return "admin/statistics/index";
    }

    @GetMapping("/admin/statistics/export.csv")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        byte[] csv = adminStatisticsService.exportWeeklyStatisticsCsv(startDate, endDate);
        String fileName = "todaktodot-weekly-statistics-"
                + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(fileName)
                        .build()
                        .toString())
                .contentType(new MediaType("text", "csv"))
                .body(csv);
    }
}
