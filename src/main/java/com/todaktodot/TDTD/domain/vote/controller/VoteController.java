package com.todaktodot.TDTD.domain.vote.controller;

import com.todaktodot.TDTD.domain.login.respository.entity.UserPrincipal;
import com.todaktodot.TDTD.domain.vote.dto.request.*;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteCategory;
import com.todaktodot.TDTD.domain.vote.dto.response.VoteCreateResponseDTO;
import com.todaktodot.TDTD.domain.vote.dto.response.VoteListResponseDTO;
import com.todaktodot.TDTD.domain.vote.dto.response.VoteResponseDTO;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteSortCondition;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteStatus;
import com.todaktodot.TDTD.domain.vote.service.VoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/votes")
@Tag(name="투표", description = "투표 API")
public class VoteController {
    private final VoteService voteService;

    /**
     * 투표 목록 조회
     */
    @Operation(description = "투표 목록 조회 API")
    @ApiResponse(responseCode = "200", description = "투표 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = VoteListResponseDTO.class)))
    @GetMapping("/list")
    public ResponseEntity<VoteListResponseDTO> getList(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                       @RequestParam(name = "category", required = false) List<VoteCategory> categories,
                                                       @RequestParam(name = "status", required = false) VoteStatus status,
                                                       @RequestParam(name = "isMine", required = false) String isMine,
                                                       @RequestParam(name = "sortBy") VoteSortCondition sortBy,
                                                       @RequestParam(name = "cursor", required = false) String cursor,
                                                       @RequestParam(name = "size", required = false, defaultValue = "10") int size) {
        VoteListResponseDTO response = voteService.getList(userPrincipal.getId(), categories, status, isMine, sortBy, cursor, size);
        return ResponseEntity.ok(response);
    }

    /**
     * 투표 단건 조회
     */
    @Operation(description = "투표 단건 조회 API")
    @ApiResponse(responseCode = "200", description = "투표 단건 조회 성공",
            content = @Content(schema = @Schema(implementation = VoteResponseDTO.class)))
    @GetMapping()
    public ResponseEntity<VoteResponseDTO> getDetail(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                   @RequestParam(name = "voteId") Long voteId) {
        VoteResponseDTO response = voteService.getDetail(userPrincipal.getId(), voteId);
        return ResponseEntity.ok(response);
    }

    /**
     * 투표 생성
     */
    @Operation(description = "투표 생성 API")
    @ApiResponse(responseCode = "200", description = "투표 생성 성공",
            content = @Content(schema = @Schema(implementation = VoteCreateResponseDTO.class)))
    @PostMapping()
    public ResponseEntity<VoteCreateResponseDTO> create(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                        @RequestBody VoteCreateRequestDTO request) {
        VoteCreateResponseDTO response = voteService.create(userPrincipal.getId(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * 투표 수정
     */
    @Operation(description = "투표 수정 API")
    @ApiResponse(responseCode = "200", description = "투표 수정 성공")
    @PutMapping()
    public ResponseEntity<Void> update(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                        @RequestBody VoteUpdateRequestDTO request) {
        voteService.update(userPrincipal.getId(), request);
        return ResponseEntity.ok().build();
    }

    /**
     * 투표 삭제
     */
    @Operation(description = "투표 삭제 API")
    @ApiResponse(responseCode = "200", description = "투표 삭제 성공")
    @DeleteMapping("/{voteId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                       @PathVariable Long voteId) {
        voteService.delete(userPrincipal.getId(), voteId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "투표 참여", description = "투표에 참여합니다. 이미 참여한 상태에서 다른 항목을 보내면 재투표로 처리됩니다.")
    @ApiResponse(responseCode = "200", description = "참여 성공",
            content = @Content(schema = @Schema(implementation = VoteResponseDTO.class)))
    @PostMapping("/select")
    public ResponseEntity<VoteResponseDTO> select(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                  @Valid @RequestBody VoteSelectRequestDTO requestDTO) {

        VoteResponseDTO response = voteService.select(userPrincipal.getId(), requestDTO);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "투표 참여 취소", description = "참여한 투표를 취소하고 미투표 상태로 되돌립니다.")
    @ApiResponse(responseCode = "200", description = "취소 성공",
            content = @Content(schema = @Schema(implementation = VoteResponseDTO.class)))
    @DeleteMapping("/select")
    public ResponseEntity<VoteResponseDTO> cancelSelect(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                        @RequestParam(name = "voteId") Long voteId) {

        VoteResponseDTO response = voteService.cancelSelect(userPrincipal.getId(), voteId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "투표 좋아요", description = "투표에 좋아요를 등록합니다.")
    @ApiResponse(responseCode = "200", description = "좋아요 성공")
    @PostMapping("/like")
    public ResponseEntity<Void> like(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                     @Valid @RequestBody VoteLikeRequestDTO requestDTO) {

        voteService.like(userPrincipal.getId(), requestDTO.getVoteId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "투표 좋아요 취소", description = "등록한 좋아요를 취소합니다.")
    @ApiResponse(responseCode = "200", description = "취소 성공")
    @DeleteMapping("/like")
    public ResponseEntity<Void> cancelLike(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                           @RequestParam(name = "voteId") Long voteId) {

        voteService.cancelLike(userPrincipal.getId(), voteId);
        return ResponseEntity.ok().build();
    }

    /**
     * 투표 신고하기
     */
    @Operation(description = "투표 신고 API")
    @ApiResponse(responseCode = "200", description = "투표 신고 성공")
    @PostMapping("/reports")
    public ResponseEntity<Void> report(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                       @RequestBody VoteReportRequestDTO request) {
        voteService.report(userPrincipal.getId(), request);
        return ResponseEntity.ok().build();
    }
}
