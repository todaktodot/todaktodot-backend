package com.todaktodot.TDTD.domain.insight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class AiGeneratedInsightDTO {

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("economy_part")
    private String economyPart;

    @JsonProperty("lifestyle_part")
    private String lifestylePart;

    @JsonProperty("love_part")
    private String lovePart;
}
