package com.todaktodot.TDTD.domain.vote.controller;

import com.todaktodot.TDTD.domain.login.respository.entity.UserPrincipal;
import com.todaktodot.TDTD.domain.vote.dto.request.VoteLikeRequestDTO;
import com.todaktodot.TDTD.domain.vote.service.VoteLikeService;
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
@Tag(name = "투표 좋아요", description = "투표 좋아요 및 취소 API")
public class VoteLikeController {

    private final VoteLikeService voteLikeService;

    @Operation(summary = "투표 좋아요", description = "투표에 좋아요를 등록합니다.")
    @ApiResponse(responseCode = "200", description = "좋아요 성공")
    @PostMapping("/like")
    public ResponseEntity<Void> like(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody VoteLikeRequestDTO requestDTO) {

        voteLikeService.like(userPrincipal.getId(), requestDTO.getVoteId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "투표 좋아요 취소", description = "등록한 좋아요를 취소합니다.")
    @ApiResponse(responseCode = "200", description = "취소 성공")
    @DeleteMapping("/like")
    public ResponseEntity<Void> cancelLike(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(name = "voteId") Long voteId) {

        voteLikeService.cancelLike(userPrincipal.getId(), voteId);
        return ResponseEntity.ok().build();
    }
}
