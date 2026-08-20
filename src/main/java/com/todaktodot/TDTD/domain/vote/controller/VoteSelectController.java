package com.todaktodot.TDTD.domain.vote.controller;

import com.todaktodot.TDTD.domain.login.respository.entity.UserPrincipal;
import com.todaktodot.TDTD.domain.vote.dto.request.VoteSelectRequestDTO;
import com.todaktodot.TDTD.domain.vote.dto.response.VoteResponseDTO;
import com.todaktodot.TDTD.domain.vote.service.VoteSelectService;
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
@RequestMapping("/api/votes")
@Tag(name = "투표 참여", description = "투표 참여 및 취소 API")
public class VoteSelectController {

    private final VoteSelectService voteSelectService;

    @Operation(summary = "투표 참여", description = "투표에 참여합니다. 이미 참여한 상태에서 다른 항목을 보내면 재투표로 처리됩니다.")
    @ApiResponse(responseCode = "200", description = "참여 성공")
    @PostMapping("/select")
    public ResponseEntity<VoteResponseDTO> select(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody VoteSelectRequestDTO requestDTO) {

        VoteResponseDTO response = voteSelectService.select(userPrincipal.getId(), requestDTO);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "투표 참여 취소", description = "참여한 투표를 취소하고 미투표 상태로 되돌립니다.")
    @ApiResponse(responseCode = "200", description = "취소 성공")
    @DeleteMapping("/select")
    public ResponseEntity<VoteResponseDTO> cancelSelect(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(name = "voteId") Long voteId) {

        VoteResponseDTO response = voteSelectService.cancelSelect(userPrincipal.getId(), voteId);
        return ResponseEntity.ok(response);
    }
}
