package com.todaktodot.TDTD.domain.vote.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class VoteCandidateApproveResultDTO {

    private final int approvedCount;

    // 검증에 걸려 등록하지 못한 건들. 화면에 사유를 그대로 보여준다.
    private final List<String> failures;
}
