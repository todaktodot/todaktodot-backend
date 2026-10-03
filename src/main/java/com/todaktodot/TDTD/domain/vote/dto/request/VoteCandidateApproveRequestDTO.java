package com.todaktodot.TDTD.domain.vote.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class VoteCandidateApproveRequestDTO {

    private Long candidateId;

    // 어드민이 화면에서 고친 값. 비어 있으면 저장된 후보 내용을 그대로 쓴다.
    private String title;
    private List<String> options;
}
