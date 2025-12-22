package com.todaktodot.TDTD.domain.profile.controller;

import com.todaktodot.TDTD.domain.login.respository.entity.UserPrincipal;
import com.todaktodot.TDTD.domain.profile.dto.request.SetNicknameRequestDTO;
import com.todaktodot.TDTD.domain.profile.dto.response.SetNicknameResponseDTO;
import com.todaktodot.TDTD.domain.profile.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    @Operation(description = "닉네임 설정")
    @ApiResponse(responseCode = "200", description = "닉네임 설정 성공")
    @PatchMapping("/nickname")
    public ResponseEntity<SetNicknameResponseDTO> setNickname(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody SetNicknameRequestDTO requestDTO) {

        SetNicknameResponseDTO response = profileService.setNickname(userPrincipal.getId(), requestDTO);
        return ResponseEntity.ok(response);
    }
}