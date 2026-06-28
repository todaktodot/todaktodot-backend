package com.todaktodot.TDTD.domain.profile.controller;

import com.todaktodot.TDTD.domain.login.dto.request.LoginRequestDTO;
import com.todaktodot.TDTD.domain.login.respository.entity.UserPrincipal;
import com.todaktodot.TDTD.domain.profile.dto.request.SetNicknameRequestDTO;
import com.todaktodot.TDTD.domain.profile.dto.request.SetOnboardingRequestDTO;
import com.todaktodot.TDTD.domain.profile.dto.response.SetNicknameResponseDTO;
import com.todaktodot.TDTD.domain.profile.dto.response.SetOnboardingResponseDTO;
import com.todaktodot.TDTD.domain.profile.dto.response.UserDetailResponseDTO;
import com.todaktodot.TDTD.domain.profile.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
@Tag(name = "회원 정보", description = "회원 정보 관리 API")
public class ProfileController {

    private final ProfileService profileService;

    @Operation(summary = "온보딩 - 닉네임, 생년월일, 성별 설정", description = "로그인한 회원의 닉네임, 생년월일, 성별을 설정합니다")
    @ApiResponse(responseCode = "200", description = "온보딩 정보 설정 성공")
    @PatchMapping("/onboarding")
    public ResponseEntity<SetOnboardingResponseDTO> setNickname(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody SetOnboardingRequestDTO requestDTO) {

        SetOnboardingResponseDTO response = profileService.setOnboarding(userPrincipal.getId(), requestDTO);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "닉네임 설정", description = "로그인한 회원의 닉네임을 설정합니다")
    @ApiResponse(responseCode = "200", description = "닉네임 설정 성공")
    @PatchMapping("/nickname")
    public ResponseEntity<SetNicknameResponseDTO> setNickname(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody SetNicknameRequestDTO requestDTO) {

        SetNicknameResponseDTO response = profileService.setNickname(userPrincipal.getId(), requestDTO);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "회원정보 조회", description = "로그인한 회원의 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "회원 정보 조회 성공",
            content = @Content(schema = @Schema(implementation = UserDetailResponseDTO.class)))
    @GetMapping("/detail")
    public ResponseEntity<UserDetailResponseDTO> getDetail(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        long userId = userPrincipal.getId();
        UserDetailResponseDTO response = profileService.getDetail(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "회원탈퇴", description = "로그인한 사용자를 회원탈퇴 합니다")
    @ApiResponse(responseCode = "200", description = "회원탈퇴 성공")
    @PostMapping("/withdraw")
    public ResponseEntity<HttpStatus> withdraw(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        long userId = userPrincipal.getId();
        profileService.withdraw(userId);
        return new ResponseEntity(HttpStatus.OK);
    }
}