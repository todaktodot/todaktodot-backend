package com.todaktodot.TDTD.domain.couple.controller;

import com.todaktodot.TDTD.domain.couple.dto.request.UpdateCoupleInfoRequestDTO;
import com.todaktodot.TDTD.domain.couple.dto.response.CoupleInfoResponseDTO;
import com.todaktodot.TDTD.domain.couple.service.CoupleService;
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
@RequestMapping("/api/couple")
@Tag(name = "커플 기본정보", description = "커플 기본정보 관리 API")
public class CoupleController {

    private final CoupleService coupleService;

    @Operation(summary = "커플 기본정보 수정", description = "우리가 만난 날, 관계 단계를 수정합니다")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @PatchMapping("/info")
    public ResponseEntity<CoupleInfoResponseDTO> updateCoupleInfo(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UpdateCoupleInfoRequestDTO requestDTO) {

        CoupleInfoResponseDTO response = coupleService.updateCoupleInfo(
                userPrincipal.getId(),
                requestDTO
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "커플 기본정보 조회", description = "현재 커플의 기본정보를 조회합니다")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/info")
    public ResponseEntity<CoupleInfoResponseDTO> getCoupleInfo(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        CoupleInfoResponseDTO response = coupleService.getCoupleInfo(
                userPrincipal.getId()
        );

        return ResponseEntity.ok(response);
    }
}
