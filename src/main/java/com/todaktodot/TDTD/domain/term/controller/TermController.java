package com.todaktodot.TDTD.domain.term.controller;

import com.todaktodot.TDTD.domain.login.respository.entity.UserPrincipal;
import com.todaktodot.TDTD.domain.term.dto.request.TermRequestDTO;
import com.todaktodot.TDTD.domain.term.service.TermService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/term")
@Tag(name="약관동의", description = "약관 동의 API")
public class TermController {

    private final TermService termService;

    /**
     * 약관 동의 저장 API
     */
    @Operation(description = "약관동의 저장 API")
    @ApiResponse(responseCode = "200", description = "약관 동의 저장 성공")
    @PostMapping
    public ResponseEntity<HttpStatus> saveTerm(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody TermRequestDTO termRequestDTO) {
        termRequestDTO.setUserId(userPrincipal.getId());
        termService.saveTerm(termRequestDTO);
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
