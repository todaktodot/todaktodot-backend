package com.todaktodot.TDTD.domain.vote.service;

import com.todaktodot.TDTD.domain.vote.repository.VoteLikeRepository;
import com.todaktodot.TDTD.domain.vote.repository.VoteRepository;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteDisplayStatus;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteEntity;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteLikeEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// TODO 에러 코드 체계 도입 시 VoteException 으로 교체 (V1005 숨김 / V1007 삭제)
@Service
@RequiredArgsConstructor
public class VoteLikeServiceImpl implements VoteLikeService {

    private final VoteRepository voteRepository;
    private final VoteLikeRepository voteLikeRepository;

    @Override
    @Transactional
    public void like(Long userId, Long voteId) {

        validateLikeable(voteId);

        // 이미 좋아요한 상태에서의 재요청은 에러 없이 넘김
        if (voteLikeRepository.findByVoteIdAndUserIdAndDelYn(voteId, userId, "N").isEmpty()) {
            voteLikeRepository.save(VoteLikeEntity.builder()
                    .voteId(voteId)
                    .userId(userId)
                    .regrId(userId)
                    .build());
        }
    }

    @Override
    @Transactional
    public void cancelLike(Long userId, Long voteId) {

        validateLikeable(voteId);

        // 좋아요하지 않은 상태에서의 취소 요청은 에러 없이 넘김
        voteLikeRepository.findByVoteIdAndUserIdAndDelYn(voteId, userId, "N")
                .ifPresent(like -> like.softDelete(userId));
    }

    /**
     * 좋아요 가능한 투표인지 검증
     */
    private void validateLikeable(Long voteId) {

        VoteEntity vote = voteRepository.findByVoteIdForUpdate(voteId)
                .orElseThrow(() -> new IllegalStateException("이미 삭제된 투표입니다."));

        if (vote.getStatus() == VoteDisplayStatus.HIDDEN) {
            throw new IllegalStateException("신고 누적으로 숨김 처리된 투표입니다.");
        }
    }

}
