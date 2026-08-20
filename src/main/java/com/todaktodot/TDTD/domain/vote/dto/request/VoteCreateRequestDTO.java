package com.todaktodot.TDTD.domain.vote.dto.request;

import com.todaktodot.TDTD.domain.vote.repository.entity.VoteCategory;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VoteCreateRequestDTO {

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
