package com.todaktodot.TDTD.domain.vote.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "투표 좋아요 요청")
public class VoteLikeRequestDTO {

    @NotNull(message = "투표 ID는 필수입니다.")
    @Schema(description = "투표 ID", example = "101")
    private Long voteId;
}
