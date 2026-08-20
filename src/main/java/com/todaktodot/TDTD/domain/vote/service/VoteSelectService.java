package com.todaktodot.TDTD.domain.vote.service;

import com.todaktodot.TDTD.domain.vote.dto.request.VoteSelectRequestDTO;
import com.todaktodot.TDTD.domain.vote.dto.response.VoteResponseDTO;

public interface VoteSelectService {

    VoteResponseDTO select(Long userId, VoteSelectRequestDTO requestDTO);

    VoteResponseDTO cancelSelect(Long userId, Long voteId);
}
