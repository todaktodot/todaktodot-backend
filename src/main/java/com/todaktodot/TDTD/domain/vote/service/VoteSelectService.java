package com.todaktodot.TDTD.domain.vote.service;

import com.todaktodot.TDTD.domain.vote.dto.request.VoteSelectRequestDTO;

public interface VoteSelectService {

    // TODO 투표 참여 후 갱신된 투표 카드 반환. 목록 조회 응답 DTO 구현 후 반환 타입 변경 예정
    void select(Long userId, VoteSelectRequestDTO requestDTO);

    void cancelSelect(Long userId, Long voteId);
}
