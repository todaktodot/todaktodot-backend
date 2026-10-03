package com.todaktodot.TDTD.domain.vote.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todaktodot.TDTD.admin.prompt.repository.AiPromptRepository;
import com.todaktodot.TDTD.domain.vote.dto.request.VoteCandidateApproveRequestDTO;
import com.todaktodot.TDTD.domain.vote.dto.request.VoteCreateRequestDTO;
import com.todaktodot.TDTD.domain.vote.dto.response.VoteCandidateApproveResultDTO;
import com.todaktodot.TDTD.domain.vote.dto.response.VoteCandidateDTO;
import com.todaktodot.TDTD.domain.vote.dto.response.VoteCreateResponseDTO;
import com.todaktodot.TDTD.domain.vote.repository.VoteCandidateRepository;
import com.todaktodot.TDTD.domain.vote.repository.VoteRepository;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteCandidateEntity;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteCandidateStatus;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteCategory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("투표 AI 후보 서비스 테스트")
class VoteCandidateServiceImplTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private VoteCandidateRepository voteCandidateRepository;

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private AiPromptRepository aiPromptRepository;

    @Mock
    private VoteService voteService;

    //JSON 직렬화는 실제 동작을 그대로 검증한다.
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private VoteCandidateServiceImpl voteCandidateService;

    private static final String ACTOR = "admin";

    private VoteCandidateEntity pendingCandidate(String title, String optionsJson) {
        return VoteCandidateEntity.builder()
                .batchKey("2026-01-01")
                .category(VoteCategory.LOVE)
                .title(title)
                .optionsJson(optionsJson)
                .promptId(1L)
                .aiModel("gpt-5.4")
                .regrId(0L)
                .build();
    }

    private VoteCandidateApproveRequestDTO approveRequest(Long candidateId, String title, List<String> options) {
        VoteCandidateApproveRequestDTO request = new VoteCandidateApproveRequestDTO();
        request.setCandidateId(candidateId);
        request.setTitle(title);
        request.setOptions(options);
        return request;
    }

    @Test
    @DisplayName("후보 등록 성공 - 어드민이 고친 문구로 게시된다")
    void approve_Success_UsesEditedContent() {
        // Given
        VoteCandidateEntity candidate = pendingCandidate("원래 질문인가요?", "[\"원래1\",\"원래2\"]");

        when(voteCandidateRepository.findByCandidateIdAndDelYn(anyLong(), anyString()))
                .thenReturn(Optional.of(candidate));
        when(voteService.createBySystem(any(VoteCreateRequestDTO.class)))
                .thenReturn(VoteCreateResponseDTO.builder().voteId(100L).build());

        // When
        VoteCandidateApproveResultDTO result = voteCandidateService.approve(
                List.of(approveRequest(1L, "고친 질문인가요?", List.of("고침1", "고침2"))), ACTOR);

        // Then
        assertThat(result.getApprovedCount()).isEqualTo(1);
        assertThat(result.getFailures()).isEmpty();

        //화면에서 고친 값이 그대로 게시되는지 확인
        ArgumentCaptor<VoteCreateRequestDTO> captor = ArgumentCaptor.forClass(VoteCreateRequestDTO.class);
        verify(voteService).createBySystem(captor.capture());

        VoteCreateRequestDTO created = captor.getValue();
        assertThat(created.getTitle()).isEqualTo("고친 질문인가요?");
        assertThat(created.getOptions()).hasSize(2);
        assertThat(created.getOptions().get(0).getContent()).isEqualTo("고침1");
        assertThat(created.getOptions().get(0).getOrder()).isEqualTo(1);
        assertThat(created.getOptions().get(1).getOrder()).isEqualTo(2);

        assertThat(candidate.getStatus()).isEqualTo(VoteCandidateStatus.APPROVED);
        assertThat(candidate.getVoteId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("선택지가 빈 배열로 와도 저장된 원본으로 되돌아가지 않는다")
    void approve_EmptyOptionList_DoesNotFallBackToStored() {
        // Given
        VoteCandidateEntity candidate = pendingCandidate("질문인가요?", "[\"원래1\",\"원래2\"]");

        when(voteCandidateRepository.findByCandidateIdAndDelYn(anyLong(), anyString()))
                .thenReturn(Optional.of(candidate));

        // When - 선택지가 하나도 오지 않은 상황 (빈 배열)
        VoteCandidateApproveResultDTO result = voteCandidateService.approve(
                List.of(approveRequest(1L, "질문인가요?", List.of())), ACTOR);

        // Then - 저장된 원본("원래1","원래2")으로 게시되면 안 된다
        assertThat(result.getApprovedCount()).isZero();
        assertThat(result.getFailures()).hasSize(1);
        assertThat(result.getFailures().get(0)).contains("2~5개");

        verify(voteService, never()).createBySystem(any());
        assertThat(candidate.getStatus()).isEqualTo(VoteCandidateStatus.PENDING);
    }

    @Test
    @DisplayName("선택지를 빈 값으로 지우면 검증에 걸린다")
    void approve_BlankOptions_Fails() {
        // Given
        VoteCandidateEntity candidate = pendingCandidate("질문인가요?", "[\"원래1\",\"원래2\"]");

        when(voteCandidateRepository.findByCandidateIdAndDelYn(anyLong(), anyString()))
                .thenReturn(Optional.of(candidate));

        // When - 어드민이 선택지 입력란을 전부 비운 상황
        VoteCandidateApproveResultDTO result = voteCandidateService.approve(
                List.of(approveRequest(1L, "질문인가요?", List.of("", ""))), ACTOR);

        // Then
        assertThat(result.getApprovedCount()).isZero();
        assertThat(result.getFailures().get(0)).contains("선택지가 비었거나");
        verify(voteService, never()).createBySystem(any());
    }

    @Test
    @DisplayName("선택지가 20자를 넘으면 등록하지 않는다")
    void approve_TooLongOption_Fails() {
        // Given
        VoteCandidateEntity candidate = pendingCandidate("질문인가요?", "[\"원래1\",\"원래2\"]");

        when(voteCandidateRepository.findByCandidateIdAndDelYn(anyLong(), anyString()))
                .thenReturn(Optional.of(candidate));

        // When
        VoteCandidateApproveResultDTO result = voteCandidateService.approve(
                List.of(approveRequest(1L, "질문인가요?", List.of("가".repeat(21), "짧은선택지"))), ACTOR);

        // Then
        assertThat(result.getApprovedCount()).isZero();
        assertThat(result.getFailures().get(0)).contains("20자");
        verify(voteService, never()).createBySystem(any());
    }

    @Test
    @DisplayName("선택지가 1개면 등록하지 않는다")
    void approve_TooFewOptions_Fails() {
        // Given
        VoteCandidateEntity candidate = pendingCandidate("질문인가요?", "[\"원래1\",\"원래2\"]");

        when(voteCandidateRepository.findByCandidateIdAndDelYn(anyLong(), anyString()))
                .thenReturn(Optional.of(candidate));

        // When
        VoteCandidateApproveResultDTO result = voteCandidateService.approve(
                List.of(approveRequest(1L, "질문인가요?", List.of("하나뿐"))), ACTOR);

        // Then
        assertThat(result.getApprovedCount()).isZero();
        assertThat(result.getFailures().get(0)).contains("2~5개");
        verify(voteService, never()).createBySystem(any());
    }

    @Test
    @DisplayName("이미 등록한 후보는 다시 등록하지 않는다")
    void approve_AlreadyApproved_Skipped() {
        // Given
        VoteCandidateEntity candidate = pendingCandidate("질문인가요?", "[\"선택1\",\"선택2\"]");
        candidate.approve(100L, 0L);

        when(voteCandidateRepository.findByCandidateIdAndDelYn(anyLong(), anyString()))
                .thenReturn(Optional.of(candidate));

        // When
        VoteCandidateApproveResultDTO result = voteCandidateService.approve(
                List.of(approveRequest(1L, "질문인가요?", List.of("선택1", "선택2"))), ACTOR);

        // Then
        assertThat(result.getApprovedCount()).isZero();
        assertThat(result.getFailures().get(0)).contains("이미 처리된 후보");
        verify(voteService, never()).createBySystem(any());
    }

    @Test
    @DisplayName("검증에 걸린 건만 건너뛰고 나머지는 등록한다")
    void approve_PartialFailure_RegistersValidOnes() {
        // Given
        VoteCandidateEntity valid = pendingCandidate("정상 질문인가요?", "[\"선택1\",\"선택2\"]");
        VoteCandidateEntity invalid = pendingCandidate("문제 질문인가요?", "[\"선택1\",\"선택2\"]");

        when(voteCandidateRepository.findByCandidateIdAndDelYn(1L, "N")).thenReturn(Optional.of(valid));
        when(voteCandidateRepository.findByCandidateIdAndDelYn(2L, "N")).thenReturn(Optional.of(invalid));
        when(voteService.createBySystem(any(VoteCreateRequestDTO.class)))
                .thenReturn(VoteCreateResponseDTO.builder().voteId(200L).build());

        // When - 2번은 선택지가 1개라 검증에 걸린다
        VoteCandidateApproveResultDTO result = voteCandidateService.approve(
                List.of(
                        approveRequest(1L, "정상 질문인가요?", List.of("선택1", "선택2")),
                        approveRequest(2L, "문제 질문인가요?", List.of("하나뿐"))
                ), ACTOR);

        // Then
        assertThat(result.getApprovedCount()).isEqualTo(1);
        assertThat(result.getFailures()).hasSize(1);
        assertThat(valid.getStatus()).isEqualTo(VoteCandidateStatus.APPROVED);
        assertThat(invalid.getStatus()).isEqualTo(VoteCandidateStatus.PENDING);
    }

    @Test
    @DisplayName("없는 후보 ID 는 사유를 돌려준다")
    void approve_NotFound_Fails() {
        // Given
        when(voteCandidateRepository.findByCandidateIdAndDelYn(anyLong(), anyString()))
                .thenReturn(Optional.empty());

        // When
        VoteCandidateApproveResultDTO result = voteCandidateService.approve(
                List.of(approveRequest(999L, "질문인가요?", List.of("선택1", "선택2"))), ACTOR);

        // Then
        assertThat(result.getApprovedCount()).isZero();
        assertThat(result.getFailures().get(0)).contains("찾을 수 없습니다");
    }

    @Test
    @DisplayName("후보 반려 - PENDING 인 건만 처리한다")
    void reject_OnlyPending() {
        // Given
        VoteCandidateEntity pending = pendingCandidate("질문인가요?", "[\"선택1\",\"선택2\"]");
        VoteCandidateEntity approved = pendingCandidate("이미등록 질문인가요?", "[\"선택1\",\"선택2\"]");
        approved.approve(100L, 0L);

        when(voteCandidateRepository.findByCandidateIdAndDelYn(1L, "N")).thenReturn(Optional.of(pending));
        when(voteCandidateRepository.findByCandidateIdAndDelYn(2L, "N")).thenReturn(Optional.of(approved));

        // When
        int rejectedCount = voteCandidateService.reject(List.of(1L, 2L), ACTOR);

        // Then
        assertThat(rejectedCount).isEqualTo(1);
        assertThat(pending.getStatus()).isEqualTo(VoteCandidateStatus.REJECTED);
        assertThat(approved.getStatus()).isEqualTo(VoteCandidateStatus.APPROVED);
    }

    @Test
    @DisplayName("대기 목록 조회 - 저장된 JSON 을 선택지 목록으로 돌려준다")
    void getPendingList_ParsesOptions() {
        // Given
        VoteCandidateEntity candidate = pendingCandidate("질문인가요?", "[\"선택1\",\"선택2\",\"선택3\"]");

        when(voteCandidateRepository.findAllByStatusAndDelYnOrderByRegDtDescCandidateIdDesc(
                VoteCandidateStatus.PENDING, "N")).thenReturn(List.of(candidate));

        // When
        List<VoteCandidateDTO> result = voteCandidateService.getPendingList();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("질문인가요?");
        assertThat(result.get(0).getOptions()).containsExactly("선택1", "선택2", "선택3");
        assertThat(result.get(0).getCategory()).isEqualTo("LOVE");
        assertThat(result.get(0).getCategoryLabel()).isEqualTo("연애관");
    }
}
