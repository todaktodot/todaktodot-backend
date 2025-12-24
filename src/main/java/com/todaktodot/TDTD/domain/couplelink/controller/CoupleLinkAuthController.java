package com.todaktodot.TDTD.domain.couplelink.controller;

import com.todaktodot.TDTD.domain.couplelink.dto.request.ConnectLinkCodeRequestDTO;
import com.todaktodot.TDTD.domain.couplelink.dto.response.ConnectLinkCodeResponseDTO;
import com.todaktodot.TDTD.domain.couplelink.dto.response.IssueLinkCodeResponseDTO;
import com.todaktodot.TDTD.domain.couplelink.service.CoupleLinkAuthService;
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
@RequestMapping("/api/couple-link")
@Tag(name = "커플 연결", description = "커플 연결, 커플 연결 코드 관련 API")
public class CoupleLinkAuthController {

    private final CoupleLinkAuthService coupleLinkAuthService;

    @Operation(summary = "커플 연결 코드 발급", description = "커플 연결을 위한 커플 연결 코드를 발급합니다")
    @ApiResponse(responseCode = "200", description = "발급 성공")
    @PostMapping("/issue")
    public ResponseEntity<IssueLinkCodeResponseDTO> issueLinkCode(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        IssueLinkCodeResponseDTO response = coupleLinkAuthService.issueLinkCode(userPrincipal.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "커플 연결 코드로 커플 연결", description = "미리 발급된 커플 연결 코드로, 발급자와 입력자를 커플 관계로 연결합니다")
    @ApiResponse(responseCode = "200", description = "연결 성공")
    @PostMapping("/connect")
    public ResponseEntity<ConnectLinkCodeResponseDTO> connectLinkCode(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ConnectLinkCodeRequestDTO requestDTO) {

        ConnectLinkCodeResponseDTO response = coupleLinkAuthService.connectLinkCode(userPrincipal.getId(), requestDTO);
        return ResponseEntity.ok(response);
    }
}