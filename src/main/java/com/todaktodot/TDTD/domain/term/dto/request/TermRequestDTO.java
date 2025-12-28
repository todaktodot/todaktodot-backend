package com.todaktodot.TDTD.domain.term.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 약관동의 저장 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class TermRequestDTO {

    @JsonIgnore
    private Long userId;

    @Schema(description = "마케팅 및 앱 알림 동의 여부")
    private String marketingAndAlarmYN;
}
