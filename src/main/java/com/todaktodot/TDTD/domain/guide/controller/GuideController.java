package com.todaktodot.TDTD.domain.guide.controller;

import com.todaktodot.TDTD.domain.guide.dto.response.GuideResponseDTO;
import com.todaktodot.TDTD.domain.guide.service.GuideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/guide")
@Tag(name="가이트 툴팁", description = "가이드 툴팁 API")
public class GuideController {

    private final GuideService guideService;

    /**
     * 가이드 툴팁 조회 API
     */
    @Operation(description = "가이드 툽팁 조회 API")
    @ApiResponse(responseCode = "200", description = "가이드 툴팁 조회 성공")
    @GetMapping
    public ResponseEntity<GuideResponseDTO> getGuide() {
        GuideResponseDTO response = guideService.getGuide();
        return ResponseEntity.ok(response);
    }
}
