package com.todaktodot.TDTD.domain.vote.service;

import com.todaktodot.TDTD.domain.vote.dto.request.VoteSelectRequestDTO;
import com.todaktodot.TDTD.domain.vote.dto.response.VoteResponseDTO;
import com.todaktodot.TDTD.domain.vote.repository.VoteOptionRepository;
import com.todaktodot.TDTD.domain.vote.repository.VoteRepository;
import com.todaktodot.TDTD.domain.vote.repository.VoteSelectRepository;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteDisplayStatus;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteEntity;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteSelectEntity;
import com.todaktodot.TDTD.domain.vote.repository.projection.VoteProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// TODO 에러 코드 체계 도입 시 VoteException 으로 교체 (V1001 마감 / V1005 숨김 / V1007 삭제)
@Service
@RequiredArgsConstructor
public class VoteSelectServiceImpl implements VoteSelectService {

    private final VoteRepository voteRepository;
    private final VoteOptionRepository voteOptionRepository;
    private final VoteSelectRepository voteSelectRepository;

    @Override
    @Transactional
    public VoteResponseDTO select(Long userId, VoteSelectRequestDTO requestDTO) {

        Long voteId = requestDTO.getVoteId();
        Long optionId = requestDTO.getOptionId();

        validateParticipable(voteId);

        // 다른 투표의 옵션 ID를 보내는 요청 방어
        if (!voteOptionRepository.existsByOptionIdAndVoteIdAndDelYn(optionId, voteId, "N")) {
            throw new IllegalArgumentException("해당 투표의 답변 항목이 아닙니다.");
        }

        Optional<VoteSelectEntity> activeSelect =
                voteSelectRepository.findByVoteIdAndUserIdAndDelYn(voteId, userId, "N");

        if (activeSelect.isPresent()) {
            activeSelect.get().updateOptionId(optionId, userId);   // 재투표 - 참여 수는 변하지 않음

        } else {
            voteSelectRepository.save(VoteSelectEntity.builder()
                    .voteId(voteId)
                    .userId(userId)
                    .optionId(optionId)
                    .regrId(userId)
                    .build());

            voteRepository.increaseParticipantCnt(voteId, userId);
        }

        return getVoteResponse(voteId, userId);
    }

    @Override
    @Transactional
    public VoteResponseDTO cancelSelect(Long userId, Long voteId) {

        validateParticipable(voteId);

        // 참여하지 않은 상태에서의 취소 요청은 에러 없이 넘김
        voteSelectRepository.findByVoteIdAndUserIdAndDelYn(voteId, userId, "N")
                .ifPresent(select -> {
                    select.softDelete(userId);
                    voteRepository.decreaseParticipantCnt(voteId, userId);
                });

        return getVoteResponse(voteId, userId);
    }

    /**
     * 참여/취소 후 갱신된 투표 카드 조회
     */
    private VoteResponseDTO getVoteResponse(Long voteId, Long userId) {
        List<VoteProjection> projections = voteRepository.selectVoteDetails(List.of(voteId), userId);

        if (projections.isEmpty()) {
            return null;
        }

        String remainingTime = calculateRemainingTime(projections.get(0).getClosedAt());
        return VoteResponseDTO.from(projections, remainingTime);
    }

    /**
     * TODO 잔여 시간은 시간 단위 기준 (분 미표기), 마감 시 "마감" 고정 표기 — UX 정책 화면 확인
     */
    private static String calculateRemainingTime(LocalDateTime closedAt) {
        LocalDateTime now = LocalDateTime.now();

        if (!closedAt.isAfter(now)) {
            return "마감";
        }

        long hours = Duration.between(now, closedAt).toHours();
        return String.valueOf(hours);
    }

    /**
     * 참여 가능한 투표인지 검증
     * 화면에 남은 시간이 표시되어 있더라도 서버 시각을 기준으로 확인
     */
    private void validateParticipable(Long voteId) {

        VoteEntity vote = voteRepository.findByVoteIdForUpdate(voteId)
                .orElseThrow(() -> new IllegalStateException("이미 삭제된 투표입니다."));

        if (vote.getStatus() == VoteDisplayStatus.HIDDEN) {
            throw new IllegalStateException("신고 누적으로 숨김 처리된 투표입니다.");
        }

        if (!vote.getClosedAt().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("이미 마감된 투표입니다.");
        }
    }

}
