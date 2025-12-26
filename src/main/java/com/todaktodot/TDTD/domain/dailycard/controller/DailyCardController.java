package com.todaktodot.TDTD.domain.dailycard.controller;

import com.todaktodot.TDTD.domain.dailycard.dto.request.GenerateDailyCardRequestDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.response.GenerateDailyCardResponseDTO;
import com.todaktodot.TDTD.domain.dailycard.service.DailyCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/daily-card")
@Tag(name = "데일리카드 관리", description = "데일리카드 콘텐츠 생성 및 관리 API (관리자용)")
public class DailyCardController {

    private final DailyCardService dailyCardService;

    @Operation(summary = "AI로 데일리카드 생성", description = "질문 모드, 주제, 유형을 지정하여 AI로 데일리카드 콘텐츠를 생성합니다")
    @ApiResponse(responseCode = "200", description = "생성 성공")
    @PostMapping("/generate")
    public ResponseEntity<GenerateDailyCardResponseDTO> generateDailyCard(
            @Valid @RequestBody GenerateDailyCardRequestDTO requestDTO) {

        GenerateDailyCardResponseDTO response = dailyCardService.generateDailyCard(requestDTO);
        return ResponseEntity.ok(response);
    }
}