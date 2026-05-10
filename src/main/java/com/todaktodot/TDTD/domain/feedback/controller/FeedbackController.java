package com.todaktodot.TDTD.domain.feedback.controller;

import com.todaktodot.TDTD.domain.feedback.dto.reqeust.GenerateFeedbackRequestDTO;
import com.todaktodot.TDTD.domain.feedback.dto.response.FeedbackResponseDTO;
import com.todaktodot.TDTD.domain.feedback.repository.entity.FeedbackGenerationStatus;
import com.todaktodot.TDTD.domain.feedback.service.FeedbackService;
import com.todaktodot.TDTD.domain.login.respository.entity.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/feedback")
@Tag(name = "AI 피드백", description = "AI 피드백 콘텐츠 생성 및 관리 API")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @Operation(summary = "AI 피드백 단건 조회", description = FeedbackGenerationStatus.GET_API_SWAGGER_DESCRIPTION)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "피드백 상태 조회 성공. feedbackStatus 값에 따라 feedback 포함 여부가 달라집니다."),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "400", description = "커플 연결 없음, SOLO 커플, 접근 권한 없음 또는 카드 없음 등 요청 처리 불가")
    })
    @GetMapping("/couple-cards/{coupleCardId}")
    public ResponseEntity<FeedbackResponseDTO> getFeedback(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long coupleCardId) {

        Long userId = userPrincipal.getId();
        FeedbackResponseDTO response = feedbackService.getFeedback(userId, coupleCardId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "AI 피드백 생성", description = FeedbackGenerationStatus.GENERATE_API_SWAGGER_DESCRIPTION)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "피드백 생성 요청 처리 성공. GENERATING이면 생성 중, COMPLETED이면 생성 완료/기존 완료 피드백 반환입니다."),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "400", description = "답변 미완료, SOLO 커플, 접근 권한 없음, 카드 없음, 기존 FAILED 상태, AI 호출/파싱 실패 등 요청 처리 불가")
    })
    @PostMapping("/generate")
    public ResponseEntity<FeedbackResponseDTO> generateFeedback(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody GenerateFeedbackRequestDTO requestDTO) {

        Long userId = userPrincipal.getId();
        FeedbackResponseDTO response = feedbackService.generateFeedback(userId, requestDTO);
        return ResponseEntity.ok(response);
    }
}
