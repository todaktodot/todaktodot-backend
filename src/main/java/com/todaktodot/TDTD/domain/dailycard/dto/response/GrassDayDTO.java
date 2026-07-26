package com.todaktodot.TDTD.domain.dailycard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@Schema(description = "데일리카드 잔디 일자별 상태")

public class GrassDayDTO {
    @Schema(description = "날짜")
    private LocalDate date;

    @Schema(description = "답변 상태")
    private GrassStatus status;
}
