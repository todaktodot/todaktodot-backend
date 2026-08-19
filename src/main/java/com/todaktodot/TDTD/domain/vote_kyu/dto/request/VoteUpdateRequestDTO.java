package com.todaktodot.TDTD.domain.vote_kyu.dto.request;

import com.todaktodot.TDTD.domain.vote.repository.entity.VoteCategory;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VoteUpdateRequestDTO {

    private Long voteId;
    private VoteCategory category;
    private String title;
    private List<OptionRequest> options;

    @Getter
    @Setter
    public static class OptionRequest {

        private String content;
        private int order;
    }
}
