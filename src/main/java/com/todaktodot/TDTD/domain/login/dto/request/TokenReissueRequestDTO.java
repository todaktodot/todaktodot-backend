package com.todaktodot.TDTD.domain.login.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TokenReissueRequestDTO {
    @Schema(description = "유저ID")
    private long userId;
    @Schema(description = "리프레쉬 토큰")
    private String refreshToken;
}
