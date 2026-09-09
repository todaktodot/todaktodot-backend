package com.todaktodot.TDTD.domain.vote.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteCategory;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteDisplayStatus;
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
    private VoteDisplayStatus displayStatus;
    private VoteStatus status;
    private String title;
    private List<OptionResponseDTO> options;
    private int likeCnt;
    private int participantCnt;
    private int reportCnt;
    private String remainingTime;
    private Boolean isMine;
    private Boolean hasVoted;
    private Boolean hasLiked;
    private String createdAt;

    @Getter
    @Setter
    @Builder
    public static class OptionResponseDTO {

        private Long optionId;
        private String content;
        private Integer voteCnt;
        private BigDecimal voteRate;
        private Boolean isSelected;

        public static OptionResponseDTO from(VoteProjection option) {
            //투표 수가 0 이상이라면 득표율, 득표수 반환
            if (!option.getParticipantCnt().equals(0)) {
                return OptionResponseDTO.builder()
                        .optionId(option.getOptionId())
                        .content(option.getContent())
                        .voteCnt(option.getVoteCnt())
                        .voteRate(option.getVoteRate())
                        .isSelected("Y".equals(option.getIsSelected()))
                        .build();
            }
            //투표 수가 0 이면 득표율, 득표수 반환X
            else {
                return OptionResponseDTO.builder()
                        .optionId(option.getOptionId())
                        .content(option.getContent())
                        .voteCnt(null)
                        .voteRate(null)
                        .isSelected(false)
                        .build();
            }
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
                .displayStatus(first.getDisplayStatus())
                .options(options)
                 .build();
    }
}
