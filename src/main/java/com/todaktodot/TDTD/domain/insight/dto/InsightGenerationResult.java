package com.todaktodot.TDTD.domain.insight.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InsightGenerationResult {
    private String finalPrompt;
    private String rawResponse;
    private AiGeneratedInsightDTO parsedResponse;
    private double actualTemperatur;
}
