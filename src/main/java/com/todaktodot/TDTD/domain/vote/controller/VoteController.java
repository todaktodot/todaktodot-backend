package com.todaktodot.TDTD.domain.vote.controller;

import com.todaktodot.TDTD.domain.login.respository.entity.UserPrincipal;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteCategory;
import com.todaktodot.TDTD.domain.vote.dto.request.VoteCreateRequestDTO;
import com.todaktodot.TDTD.domain.vote.dto.request.VoteReportRequestDTO;
import com.todaktodot.TDTD.domain.vote.dto.request.VoteUpdateRequestDTO;
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
        Long userId = userPrincipal.getId();
        VoteListResponseDTO response = voteService.getList(userId, categories, status, isMine, sortBy, cursor, size);
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
        Long userId = userPrincipal.getId();
        VoteResponseDTO response = voteService.getDetail(userId, voteId);
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
        Long userId = userPrincipal.getId();
        VoteCreateResponseDTO response = voteService.create(userId, request);
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
        Long userId = userPrincipal.getId();
        voteService.update(userId, request);
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
        Long userId = userPrincipal.getId();
        voteService.delete(userId, voteId);
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
        Long userId = userPrincipal.getId();
        voteService.report(userId, request);
        return ResponseEntity.ok().build();
    }
}
