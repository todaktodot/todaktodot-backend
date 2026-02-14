package com.todaktodot.TDTD.domain.login.controller;

import com.todaktodot.TDTD.domain.login.dto.request.LoginRequestDTO;
import com.todaktodot.TDTD.domain.login.dto.request.TokenReissueRequestDTO;
import com.todaktodot.TDTD.domain.login.dto.response.LoginResponseDTO;
import com.todaktodot.TDTD.domain.login.dto.response.TokenReissueResponseDTO;
import com.todaktodot.TDTD.domain.login.respository.entity.UserPrincipal;
import com.todaktodot.TDTD.domain.login.service.LoginService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name="로그인", description = "로그인 API")
public class LoginController {

    private final LoginService loginService;

    /**
     * 소셜 로그인 - 카카오, 구글, 애플
     */
    @Operation(description = "소셜 로그인 API")
    @ApiResponse(responseCode = "200", description = "로그인 성공",
            content = @Content(schema = @Schema(implementation = LoginRequestDTO.class)))
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        LoginResponseDTO response = loginService.login(loginRequestDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * 토큰 재발급
     */
    @Operation(description = "토큰 재발급 API")
    @ApiResponse(responseCode = "200", description = "토큰 재발급 완료")
    @PostMapping("/reissue")
    public ResponseEntity<TokenReissueResponseDTO> reissue(@RequestBody @Valid TokenReissueRequestDTO tokenReissueRequestDTO) {
        TokenReissueResponseDTO response = loginService.reissue(tokenReissueRequestDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * 로그아웃
     */
    @Operation(description = "로그아웃 API")
    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    @PostMapping("/logout")
    public ResponseEntity<HttpStatus> login(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = userPrincipal.getId();
        loginService.logout(userId);
        return new  ResponseEntity(HttpStatus.OK);
    }

}
