package com.todaktodot.TDTD.domain.feedback.controller;

import com.todaktodot.TDTD.domain.feedback.dto.reqeust.GenerateFeedbackRequestDTO;
import com.todaktodot.TDTD.domain.feedback.dto.response.GenerateFeedbackResponseDTO;
import com.todaktodot.TDTD.domain.feedback.service.FeedbackService;
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
@RequestMapping("/api/feedback")
@Tag(name = "AI 피드백", description = "AI 피드백 콘텐츠 생성 및 관리 API")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @Operation(summary = "AI 피드백 생성", description = "커플 두 명의 답변 완료 후 AI 피드백을 생성합니다")
    @ApiResponse(responseCode = "200", description = "피드백 생성 성공")
    @PostMapping("/generate")
    public ResponseEntity<GenerateFeedbackResponseDTO> generateFeedback(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody GenerateFeedbackRequestDTO requestDTO) {

        Long userId = userPrincipal.getId();
        GenerateFeedbackResponseDTO response = feedbackService.generateFeedback(userId, requestDTO);
        return ResponseEntity.ok(response);
    }
}
