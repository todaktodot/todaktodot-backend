package com.todaktodot.TDTD.domain.term.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.todaktodot.TDTD.global.validation.Yn;
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

    @Yn(message = "정보성 알림 동의 여부는 Y 또는 N만 입력할 수 있습니다")
    @Schema(description = "정보성 알림 동의 여부")
    private String infoAlarmYN;

    @Yn(message = "광고성 알림 동의 여부는 Y 또는 N만 입력할 수 있습니다")
    @Schema(description = "광고성 알림 동의 여부")
    private String advertiesmentAlarmYN;

    @Yn(message = "마케팅 동의 여부는 Y 또는 N만 입력할 수 있습니다")
    @Schema(description = "마케팅 동의 여부")
    private String marketingAlarmYN;
}
