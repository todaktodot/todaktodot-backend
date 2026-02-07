package com.todaktodot.TDTD.domain.notification.controller;

import com.todaktodot.TDTD.domain.login.respository.entity.UserPrincipal;
import com.todaktodot.TDTD.domain.notification.dto.reqeust.DeviceTokenRequest;
import com.todaktodot.TDTD.domain.notification.service.DeviceTokenService;
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
@RequestMapping("/api/device-token")
@Tag(name = "디바이스 토큰", description = "FCM 푸시 알림을 위한 디바이스 토큰 관리 API")
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;

    @Operation(summary = "디바이스 토큰 등록/갱신", description = "FCM 토큰을 서버에 등록하거나 갱신합니다. 앱 실행 시 호출해주세요.")
    @ApiResponse(responseCode = "200", description = "등록/갱신 성공")
    @PostMapping
    public ResponseEntity<Void> registerToken(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody DeviceTokenRequest request) {

        deviceTokenService.registerOrUpdateToken(userPrincipal.getId(), request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "디바이스 토큰 삭제", description = "FCM 토큰을 비활성화합니다. 로그아웃 시 호출해주세요.")
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    @DeleteMapping
    public ResponseEntity<Void> deleteToken(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam String fcmToken) {

        deviceTokenService.deleteToken(userPrincipal.getId(), fcmToken);
        return ResponseEntity.ok().build();
    }
}
