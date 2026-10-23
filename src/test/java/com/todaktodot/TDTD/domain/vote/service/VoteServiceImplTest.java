package com.todaktodot.TDTD.domain.vote.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todaktodot.TDTD.domain.notification.service.FcmService;
import com.todaktodot.TDTD.domain.vote.dto.request.VoteCreateRequestDTO;
import com.todaktodot.TDTD.domain.vote.dto.request.VoteSelectRequestDTO;
import com.todaktodot.TDTD.domain.vote.dto.response.VoteCreateResponseDTO;
import com.todaktodot.TDTD.domain.vote.exception.VoteException;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteDisplayStatus;
import com.todaktodot.TDTD.global.exception.ErrorCode;
import com.todaktodot.TDTD.domain.vote.repository.VoteLikeRepository;
import com.todaktodot.TDTD.domain.vote.repository.VoteModerationLogRepository;
import com.todaktodot.TDTD.domain.vote.repository.VoteOptionRepository;
import com.todaktodot.TDTD.domain.vote.repository.VoteReportRepository;
import com.todaktodot.TDTD.domain.vote.repository.VoteRepository;
import com.todaktodot.TDTD.domain.vote.repository.VoteSelectRepository;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteCategory;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteEntity;
import com.todaktodot.TDTD.domain.vote.repository.projection.UserSuspensionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("투표 서비스 테스트")
class VoteServiceImplTest {

    @Mock
    private VoteRepository voteRepository;
    @Mock
    private VoteOptionRepository voteOptionRepository;
    @Mock
    private VoteSelectRepository voteSelectRepository;
    @Mock
    private VoteReportRepository voteReportRepository;
    @Mock
    private VoteLikeRepository voteLikeRepository;
    @Mock
    private VoteModerationLogRepository voteModerationLogRepository;
    @Mock
    private UserSuspensionRepository suspensionRepository;
    @Mock
    private FcmService fcmService;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private VoteServiceImpl voteService;

    private static final Long BOT_USER = 777L;

    @Test
    @DisplayName("시스템 투표 등록 - 설정한 봇 계정이 작성자로 저장된다")
    void createByAdmin_UsesConfiguredBotUser() {
        // Given
        ReflectionTestUtils.setField(voteService, "botUserId", BOT_USER);
        when(voteRepository.save(any(VoteEntity.class))).thenAnswer(invocation -> {
            VoteEntity saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "voteId", 500L);
            return saved;
        });

        // When
        VoteCreateResponseDTO result = voteService.createByAdmin(createRequest());

        // Then
        ArgumentCaptor<VoteEntity> captor = ArgumentCaptor.forClass(VoteEntity.class);
        verify(voteRepository).save(captor.capture());

        assertThat(result.getVoteId()).isEqualTo(500L);
        assertThat(captor.getValue().getUserId()).isEqualTo(BOT_USER);
        //감사 컬럼은 다른 시스템 생성 레코드와 같은 0 을 유지한다
        assertThat(captor.getValue().getRegrId()).isZero();
    }

    @Test
    @DisplayName("시스템 투표 등록 - 봇 계정 설정이 없으면 게시하지 않는다")
    void createByAdmin_BotUserNotConfigured_Throws() {
        // Given - vote.bot.user-id 기본값(0) 그대로인 상태
        ReflectionTestUtils.setField(voteService, "botUserId", 0L);

        // When & Then - 되돌릴 수 없는 작업이라 저장 전에 멈춘다
        assertThatThrownBy(() -> voteService.createByAdmin(createRequest()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("vote.bot.user-id");

        verify(voteRepository, never()).save(any(VoteEntity.class));
        verify(voteOptionRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("초안은 참여할 수 없다 - 마감 시각이 비어 있어도 터지지 않는다")
    void select_Draft_TreatedAsNotFound() {
        // Given
        VoteEntity draft = VoteEntity.draft(BOT_USER, "용감한 수달0001", VoteCategory.LOVE, "초안 질문?", 0L);
        when(voteRepository.findById(900L)).thenReturn(Optional.of(draft));

        VoteSelectRequestDTO request = new VoteSelectRequestDTO();
        ReflectionTestUtils.setField(request, "voteId", 900L);
        ReflectionTestUtils.setField(request, "optionId", 9001L);

        // When & Then - CLOSED_AT 이 null 이라 가드가 없으면 NPE 가 난다
        assertThatThrownBy(() -> voteService.select(1L, request))
                .isInstanceOf(VoteException.class)
                .extracting(e -> ((VoteException) e).getErrorCode())
                .isEqualTo(ErrorCode.VOTE_NOT_FOUND);

        verify(voteRepository, never()).increaseParticipantCnt(any(), any());
    }

    @Test
    @DisplayName("초안은 좋아요할 수 없다")
    void like_Draft_TreatedAsNotFound() {
        // Given
        VoteEntity draft = VoteEntity.draft(BOT_USER, "용감한 수달0001", VoteCategory.LOVE, "초안 질문?", 0L);
        when(voteRepository.findById(900L)).thenReturn(Optional.of(draft));

        // When & Then
        assertThatThrownBy(() -> voteService.like(1L, 900L))
                .isInstanceOf(VoteException.class)
                .extracting(e -> ((VoteException) e).getErrorCode())
                .isEqualTo(ErrorCode.VOTE_NOT_FOUND);
    }

    @Test
    @DisplayName("하루 10개 제한 집계에서 초안은 제외한다")
    void create_DailyLimit_ExcludesDraft() {
        // Given
        when(voteRepository.countByUserIdAndRegDtBetweenAndDelYnAndStatusNot(
                any(), any(), any(), any(), any())).thenReturn(0L);
        when(voteRepository.save(any(VoteEntity.class))).thenAnswer(invocation -> {
            VoteEntity saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "voteId", 501L);
            return saved;
        });

        // When
        voteService.create(1L, createRequest());

        // Then - 집계 대상에서 DRAFT 를 빼고 세는지 확인
        verify(voteRepository).countByUserIdAndRegDtBetweenAndDelYnAndStatusNot(
                any(), any(), any(), any(), eq(VoteDisplayStatus.DRAFT));
    }

    private VoteCreateRequestDTO createRequest() {
        VoteCreateRequestDTO request = new VoteCreateRequestDTO();
        request.setCategory(VoteCategory.LOVE);
        request.setTitle("기념일 챙기는 편이야?");
        request.setOptions(List.of(optionRequest("챙긴다", 1), optionRequest("안 챙긴다", 2)));
        return request;
    }

    private VoteCreateRequestDTO.OptionRequest optionRequest(String content, int order) {
        VoteCreateRequestDTO.OptionRequest option = new VoteCreateRequestDTO.OptionRequest();
        option.setContent(content);
        option.setOrder(order);
        return option;
    }
}
