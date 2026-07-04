package com.todaktodot.TDTD.domain.dailycard.controller;

import com.todaktodot.TDTD.domain.dailycard.dto.request.*;
import com.todaktodot.TDTD.domain.dailycard.dto.response.*;
import com.todaktodot.TDTD.domain.dailycard.service.DailyCardService;
import com.todaktodot.TDTD.domain.login.respository.entity.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/daily-card")
@Tag(name = "데일리카드", description = "데일리카드 답변 제출 및 조회 API (사용자용)")
public class DailyCardUserController {

    private final DailyCardService dailyCardService;

    @Operation(summary = "데일리카드 답변 제출", description = "데일리카드 질문에 대한 답변을 제출합니다 (객관식 필수, 주관식 선택)")
    @ApiResponse(responseCode = "200", description = "답변 저장 성공")
    @PostMapping("/answer")
    public ResponseEntity<SubmitAnswerResponseDTO> submitAnswer(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody SubmitAnswerRequestDTO requestDTO) {

        Long userId = userPrincipal.getId();
        SubmitAnswerResponseDTO response = dailyCardService.submitAnswer(userId, requestDTO);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "커플에게 데일리카드 할당", description = "특정 커플에게 데일리카드를 할당합니다 (같은 날짜에 중복 할당 불가)")
    @ApiResponse(responseCode = "200", description = "카드 할당 성공")
    @PostMapping("/assign")
    public ResponseEntity<AssignCardResponseDTO> assignCardToCouple(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody AssignCardRequestDTO requestDTO) {

        Long userId = userPrincipal.getId();
        AssignCardResponseDTO response = dailyCardService.assignCardToCouple(userId, requestDTO);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "주간 데일리카드 조회", description = "배정된 주간 데일리카드를 질문/선택지와 함께 조회합니다")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/weekly")
    public ResponseEntity<WeeklyCardResponseDTO> getWeeklyCards(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long userId = userPrincipal.getId();
        WeeklyCardResponseDTO response = dailyCardService.getWeeklyCards(userId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내 커플 데일리카드 배정",
               description = "요청 날짜 범위에 대해 데일리카드를 배정합니다. 이미 배정된 날짜는 자동 스킵됩니다.")
    @ApiResponse(responseCode = "200", description = "배정 완료")
    @PostMapping("/assign/me")
    public ResponseEntity<AssignMyCardResponseDTO> assignMyDailyCards(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long userId = userPrincipal.getId();
        AssignMyCardResponseDTO response = dailyCardService.assignMyDailyCards(userId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "데일리카드 유형 선택",
               description = "당일 배정된 2개의 데일리카드 중 하나를 선택합니다. 미선택 카드는 자동으로 삭제 처리됩니다.")
    @ApiResponse(responseCode = "200", description = "유형 선택 완료")
    @PostMapping("/select-type")
    public ResponseEntity<SelectCardTypeResponseDTO> selectCardType(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody SelectCardTypeRequestDTO requestDTO) {

        Long userId = userPrincipal.getId();
        SelectCardTypeResponseDTO response = dailyCardService.selectCardType(userId, requestDTO);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "데일리카드 답변 잔디 조회",
            description = "날짜 범위 내 현재 사용자와 상대방의 데일리카드 답변 참여 상태를 일자별로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/grass")
    public ResponseEntity<GrassResponseDTO> getGrass(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long userId = userPrincipal.getId();
        GrassResponseDTO response = dailyCardService.getGrass(userId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "히스토리 카드 리스트 조회",
               description = "날짜 범위 내 배정된 데일리카드를 일자별로 조회합니다. 유형 선택 여부에 따라 노출 정보가 달라집니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/history")
    public ResponseEntity<HistoryCardResponseDTO> getHistoryCards(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long userId = userPrincipal.getId();
        HistoryCardResponseDTO response = dailyCardService.getHistoryCards(userId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "히스토리 카드 리스트 + 상세 한 번에 조회",
               description = "날짜 범위 내 배정된 데일리카드를 일자별로 조회합니다. "
                       + "선택 완료 카드는 질문/선택지/답변/AI 피드백까지 포함합니다. "
                       + "앱 메인 진입 시 한 번의 호출로 전체 데이터를 로드합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/history/with-details")
    public ResponseEntity<HistoryDetailResponseDTO> getHistoryDetailCards(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long userId = userPrincipal.getId();
        HistoryDetailResponseDTO response = dailyCardService.getHistoryDetailCards(userId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "히스토리 카드 상세 단건 조회",
               description = "배정된 커플 데일리카드ID로 상세를 조회합니다. ")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/history/detail")
    public ResponseEntity<HistoryDetailResponseDTO> getHistoryDetailCard(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(name = "coupleCardId") Long coupleCardId) {
        Long userId = userPrincipal.getId();
        HistoryDetailResponseDTO response = dailyCardService.getHistoryDetailCard(userId, coupleCardId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "데일리카드 콕찌르기",
               description = "데일리카드를 콕찌르기 합니다.")
    @ApiResponse(responseCode = "200", description = "콕찌르기 성공")
    @PostMapping("/poke")
    public ResponseEntity<HttpStatus> pokeCoupleDailyCard(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(name = "coupleCardId") Long coupleCardId) {
        Long userId = userPrincipal.getId();
        dailyCardService.pokeCoupleDailyCard(userId, coupleCardId);
        return new ResponseEntity(HttpStatus.OK);
    }

    @Operation(summary = "데일리카드 답변 이모지 저장/수정",
            description = "데일리카드 답변에 이모지를 남깁니다.")
    @ApiResponse(responseCode = "200", description = "이모지 저장/수정 성공")
    @PostMapping("/history/emoji")
    public ResponseEntity<SaveEmojiResponseDTO> setEmojiReaction(
            @AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody SaveEmojiRequestDTO requestDTO) {
        Long userId = userPrincipal.getId();
        SaveEmojiResponseDTO response = dailyCardService.setEmojiReaction(userId, requestDTO);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "데일리카드 이모지 삭제",
            description = "데일리카드 답변에 남긴 이모지를 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "이모지 삭제 성공")
    @DeleteMapping("/history/emoji")
    public ResponseEntity<HttpStatus> deleteEmojiReaction(
            @AuthenticationPrincipal UserPrincipal userPrincipal, @RequestParam(name = "coupleCardId") Long coupleCardId) {
        Long userId = userPrincipal.getId();
        dailyCardService.deleteEmojiReaction(userId, coupleCardId);
        return new ResponseEntity(HttpStatus.OK);
    }

    @Operation(summary = "히스토리 카드 공유 링크 생성",
            description = "히스토리 카드 '공유하기' 링크를 생성합니다.")
    @ApiResponse(responseCode = "200", description = "히스토리 카드 공유 링크 생성 성공")
    @PostMapping("/history/share-link")
    public ResponseEntity<HistoryCardShareLinkResponseDTO> setHistoryCardShareLink(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody HistoryCardShareLinkRequestDTO requestDTO) {
        Long userId = userPrincipal.getId();
        HistoryCardShareLinkResponseDTO response = dailyCardService.setHistoryCardShareLink(userId, requestDTO);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "히스토리 카드 공유 링크 검증",
            description = "히스토리 카드 공유하기 링크를 검증합니다.")
    @ApiResponse(responseCode = "200", description = "히스토리 카드 공유 링크 검증 성공")
    @PostMapping("/history/share-link/validate")
    public HistoryCardShareLinkValidateResponseDTO validateHistoryCardShareLink(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody HistoryCardShareLinkValidateRequestDTO requestDTO) {
        Long userId = userPrincipal.getId();
        return dailyCardService.validateHistoryCardShareLink(userId, requestDTO);
    }
}
