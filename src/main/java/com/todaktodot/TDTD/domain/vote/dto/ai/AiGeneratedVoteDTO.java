package com.todaktodot.TDTD.domain.vote.dto.ai;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * AI가 생성한 투표 후보 1건을 매핑하는 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class AiGeneratedVoteDTO {

    private String category;  // LOVE / ECONOMY / LIFESTYLE
    private String title;
    private List<String> options;
}
