package com.todaktodot.TDTD.domain.guide.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GuideResponseDTO {
    @Schema(description = "가이드 내용")
    private String content;

    public static GuideResponseDTO of(String content) {
        return new GuideResponseDTO(content);
    }
}
