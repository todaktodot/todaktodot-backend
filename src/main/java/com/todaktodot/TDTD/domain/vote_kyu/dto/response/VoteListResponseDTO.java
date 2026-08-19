package com.todaktodot.TDTD.domain.vote_kyu.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class VoteListResponseDTO {
    private List<VoteResponseDTO> data;
    private int createVoteCnt;
    private String nextCursor;
    private boolean hasNext;
}
