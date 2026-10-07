package com.todaktodot.TDTD.domain.vote.dto.response;

import com.todaktodot.TDTD.domain.vote.repository.entity.VoteCandidateEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class VoteCandidateDTO {

    private final Long candidateId;
    private final String category;
    private final String categoryLabel;
    private final String title;
    private final List<String> options;
    private final LocalDateTime regDt;

    public static VoteCandidateDTO of(VoteCandidateEntity entity, List<String> options) {
        return VoteCandidateDTO.builder()
                .candidateId(entity.getCandidateId())
                .category(entity.getCategory().name())
                .categoryLabel(entity.getCategory().getDescription())
                .title(entity.getTitle())
                .options(options)
                .regDt(entity.getRegDt())
                .build();
    }
}
