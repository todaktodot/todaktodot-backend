package com.todaktodot.TDTD.domain.insight.controller;

import com.todaktodot.TDTD.domain.insight.dto.GenerateInsightRequestDTO;
import com.todaktodot.TDTD.domain.insight.dto.GenerateInsightResponseDTO;
import com.todaktodot.TDTD.domain.insight.service.InsightService;
import com.todaktodot.TDTD.domain.login.respository.entity.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/insight")
@Tag(name = "AI 인사이트", description = "AI 인사이트 생성 및 관리 API")
public class InsightController {

    private final InsightService insightService;

    @Operation(summary = "AI 인사이트 생성", description = "커플의 한주간 응답 데일리카드를 바탕으로 AI 인사이트를 생성합니다")
    @ApiResponse(responseCode = "200", description = "인사이트 생성 성공")
    @PostMapping("/generate")
    public ResponseEntity<GenerateInsightResponseDTO> generateFeedback(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody GenerateInsightRequestDTO requestDTO) {

        Long userId = userPrincipal.getId();
        GenerateInsightResponseDTO response = insightService.generateInsight(requestDTO);
        return ResponseEntity.ok(response);
    }
}
