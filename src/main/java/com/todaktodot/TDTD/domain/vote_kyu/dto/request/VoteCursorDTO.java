package com.todaktodot.TDTD.domain.vote_kyu.dto.request;

import com.todaktodot.TDTD.domain.vote_kyu.repository.entity.VoteSortCondition;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VoteCursorDTO {
    /**
     * 커서가 생성된 정렬 조건
     */
    private VoteSortCondition sortBy;
    /**
     * 최신순 커서
     */
    private LocalDateTime createdAt;
    /**
     * 인기순 커서
     */
    private Integer participantCnt;
    private Long voteId;
}
