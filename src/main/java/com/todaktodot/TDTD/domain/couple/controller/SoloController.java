package com.todaktodot.TDTD.domain.couple.controller;

import com.todaktodot.TDTD.domain.couple.dto.response.SoloStartResponseDTO;
import com.todaktodot.TDTD.domain.couple.service.SoloService;
import com.todaktodot.TDTD.domain.login.respository.entity.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * [TDTDBE-55] 혼자 둘러보기 API
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/solo")
@Tag(name = "혼자 둘러보기", description = "커플 연결 전 혼자 둘러보기 기능 API")
public class SoloController {

    private final SoloService soloService;

    @Operation(
            summary = "혼자 둘러보기 시작",
            description = """
                    커플 연결 없이 혼자 둘러보기를 시작합니다.
                    - 1인 커플(SOLO)이 생성됩니다
                    - 오늘 날짜로 데일리카드 2개(밸런스/상황극)가 배정됩니다
                    - 이후 커플 연결 시 기존 데이터가 유지됩니다
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "혼자 둘러보기 시작 성공"),
            @ApiResponse(responseCode = "400", description = "이미 커플 연결 또는 혼자 둘러보기 중인 경우")
    })
    @PostMapping("/start")
    public ResponseEntity<SoloStartResponseDTO> startSoloMode(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        SoloStartResponseDTO response = soloService.startSoloMode(userPrincipal.getId());

        return ResponseEntity.ok(response);
    }
}
