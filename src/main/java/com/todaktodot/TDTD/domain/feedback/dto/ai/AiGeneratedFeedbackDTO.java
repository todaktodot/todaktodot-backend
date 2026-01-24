package com.todaktodot.TDTD.domain.feedback.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;

@Getter
public class AiGeneratedFeedbackDTO {

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("match_points")
    private List<String> matchPoints;

    @JsonProperty("differences")
    private List<String> differences;

    @JsonProperty("conversation_starter")
    private String conversationStarter;
}
