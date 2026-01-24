package com.todaktodot.TDTD.domain.aireport.controller;

import com.todaktodot.TDTD.domain.aireport.dto.response.ReportDetailResponseDTO;
import com.todaktodot.TDTD.domain.aireport.dto.response.ReportListResponseDTO;
import com.todaktodot.TDTD.domain.aireport.dto.response.ReportResponseWrapDTO;
import com.todaktodot.TDTD.domain.aireport.service.ReportService;
import com.todaktodot.TDTD.domain.login.respository.entity.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai-report")
@Tag(name="AI 리포트", description = "AI 리포트 API")
public class ReportController {

    private final ReportService reportService;

    /**
     * 지난 한주 AI 리포트 생성 여부 조회 API
     */
    @Operation(description = "지난 한주 AI 리포트 생성 여부 조회 API")
    @ApiResponse(responseCode = "200", description = "지난 한주 AI 리포트 생성 여부 조회 성공",
            content = @Content(schema = @Schema(implementation = ReportResponseWrapDTO.class)))
    @GetMapping
    public ResponseEntity<ReportResponseWrapDTO> checkCreatable(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        ReportResponseWrapDTO result =  reportService.checkCreatable(userPrincipal.getId());
        return ResponseEntity.ok(result);
    }

    /**
     * AI리포트 리스트 조회
     */
    @Operation(description = "AI 리스트 조회 API - 돌아보기")
    @ApiResponse(responseCode = "200", description = "AI 리스트 조회 성공",
            content = @Content(schema = @Schema(implementation = ReportListResponseDTO.class)))
    @GetMapping("/list")
    public ResponseEntity<List<ReportListResponseDTO>> getReportList(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<ReportListResponseDTO> reportList = reportService.getReportList(userPrincipal.getId());
        return ResponseEntity.ok(reportList);
    }

    /**
     * AI리포트 상세 조회
     */
    @Operation(description = "AI리포트 상세 조회 API")
    @ApiResponse(responseCode = "200", description = "AI리포트 상세 조회 성공",
            content = @Content(schema = @Schema(implementation = ReportDetailResponseDTO.class)))
    @GetMapping("/detail/{id}")
    public ResponseEntity<ReportDetailResponseDTO> getReportDetail(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable(name = "id") Long reportId) {
        return ResponseEntity.ok(reportService.getReportDetail(userPrincipal.getId(), reportId));
    }
}
