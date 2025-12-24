package com.todaktodot.TDTD.domain.profile.controller;

import com.todaktodot.TDTD.domain.login.respository.entity.UserPrincipal;
import com.todaktodot.TDTD.domain.profile.dto.request.SetNicknameRequestDTO;
import com.todaktodot.TDTD.domain.profile.dto.response.SetNicknameResponseDTO;
import com.todaktodot.TDTD.domain.profile.service.ProfileService;
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
@RequestMapping("/api/profile")
@Tag(name = "회원 정보", description = "회원 정보 관리 API")
public class ProfileController {

    private final ProfileService profileService;

    @Operation(summary = "닉네임 설정", description = "로그인한 회원의 닉네임을 설정합니다")
    @ApiResponse(responseCode = "200", description = "닉네임 설정 성공")
    @PatchMapping("/nickname")
    public ResponseEntity<SetNicknameResponseDTO> setNickname(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody SetNicknameRequestDTO requestDTO) {

        SetNicknameResponseDTO response = profileService.setNickname(userPrincipal.getId(), requestDTO);
        return ResponseEntity.ok(response);
    }
}