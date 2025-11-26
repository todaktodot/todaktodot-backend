package com.todaktodot.TDTD.domain.login.controller;

import com.todaktodot.TDTD.domain.login.dto.request.LoginRequestDTO;
import com.todaktodot.TDTD.domain.login.dto.response.LoginTokenResponseDTO;
import com.todaktodot.TDTD.domain.login.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/login")
@Tag(name="LoginController", description = "로그인 API")
public class LoginController {

    private final LoginService loginService;

    /**
     * 소셜 로그인 - 카카오, 구글, 애플
     */
    @Operation(description = "소셜 로그인 API")
    @ApiResponse(responseCode = "200", description = "로그인 성공",
            content = @Content(schema = @Schema(implementation = LoginRequestDTO.class)))
    @PostMapping
    public ResponseEntity<LoginTokenResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        LoginTokenResponseDTO response = loginService.login(loginRequestDTO);
        return ResponseEntity.ok(response);
    }
}
