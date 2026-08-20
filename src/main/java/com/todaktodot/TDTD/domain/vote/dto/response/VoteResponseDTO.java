package com.todaktodot.TDTD.domain.vote.dto.response;

import com.todaktodot.TDTD.domain.vote.repository.entity.VoteCategory;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteStatus;
import com.todaktodot.TDTD.domain.vote.repository.projection.VoteProjection;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Getter
@Setter
@Builder
public class VoteResponseDTO {
    private Long voteId;
    private String nickname;
    private VoteCategory category;
    private VoteStatus status;
    private String title;
    private List<OptionResponseDTO> options;
    private int likeCnt;
    private int participantCnt;
    private int reportCnt;
    private String remainingTime;
    private boolean isMine;
    private boolean hasVoted;
    private boolean hasLiked;
    private String createdAt;

    @Getter
    @Setter
    @Builder
    public static class OptionResponseDTO {

        private Long optionId;
        private String content;
        private int voteCnt;
        private BigDecimal voteRate;
        private boolean isSelected;

        public static OptionResponseDTO from(VoteProjection option) {

            return OptionResponseDTO.builder()
                    .optionId(option.getOptionId())
                    .content(option.getContent())
                    .voteCnt(option.getVoteCnt())
                    .voteRate(option.getVoteRate())
                    .isSelected("Y".equals(option.getIsSelected()))
                    .build();
        }
    }

    public static VoteResponseDTO from(List<VoteProjection> rows, String remainingTime) {

        VoteProjection first = rows.get(0);

        List<OptionResponseDTO> options =
                rows.stream()
                        .sorted(Comparator.comparing(VoteProjection::getSortOrder))
                        .map(OptionResponseDTO::from)
                        .toList();

        return VoteResponseDTO.builder()
                .voteId(first.getVoteId())
                .nickname(first.getNickname())
                .category(first.getCategory())
                .status(first.getStatus())
                .title(first.getTitle())
                .likeCnt(first.getLikeCnt())
                .participantCnt(first.getParticipantCnt())
                .reportCnt(first.getReportCnt())
                .isMine("Y".equals(first.getIsMine()))
                .hasVoted("Y".equals(first.getHasVoted()))
                .hasLiked("Y".equals(first.getHasLiked()))
                .createdAt(first.getCreatedAt().toString())
                .remainingTime(remainingTime)
                .options(options)
                 .build();
    }
}
