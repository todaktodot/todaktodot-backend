package com.todaktodot.TDTD.domain.login.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {
    @Schema(description = "엑세스 토큰")
    private String accessToken;
    @Schema(description = "리프레쉬 토큰")
    private String refreshToken;
}
