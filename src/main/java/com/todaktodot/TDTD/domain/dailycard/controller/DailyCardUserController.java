package com.todaktodot.TDTD.domain.dailycard.controller;

import com.todaktodot.TDTD.domain.dailycard.dto.request.AssignCardRequestDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.request.SubmitAnswerRequestDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.response.AssignCardResponseDTO;
import com.todaktodot.TDTD.domain.dailycard.dto.response.SubmitAnswerResponseDTO;
import com.todaktodot.TDTD.domain.dailycard.service.DailyCardService;
import com.todaktodot.TDTD.domain.login.respository.entity.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/daily-card")
@Tag(name = "데일리카드", description = "데일리카드 답변 제출 및 조회 API (사용자용)")
public class DailyCardUserController {

    private final DailyCardService dailyCardService;

    @Operation(summary = "데일리카드 답변 제출", description = "데일리카드 질문에 대한 답변을 제출합니다 (객관식 필수, 주관식 선택)")
    @ApiResponse(responseCode = "200", description = "답변 저장 성공")
    @PostMapping("/answer")
    public ResponseEntity<SubmitAnswerResponseDTO> submitAnswer(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody SubmitAnswerRequestDTO requestDTO) {

        Long userId = userPrincipal.getId();
        SubmitAnswerResponseDTO response = dailyCardService.submitAnswer(userId, requestDTO);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "커플에게 데일리카드 할당", description = "특정 커플에게 데일리카드를 할당합니다 (같은 날짜에 중복 할당 불가)")
    @ApiResponse(responseCode = "200", description = "카드 할당 성공")
    @PostMapping("/assign")
    public ResponseEntity<AssignCardResponseDTO> assignCardToCouple(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody AssignCardRequestDTO requestDTO) {

        Long userId = userPrincipal.getId();
        AssignCardResponseDTO response = dailyCardService.assignCardToCouple(userId, requestDTO);
        return ResponseEntity.ok(response);
    }
}
