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
    @Schema(description = "약관동의 여부", example = "false")
    private boolean isJoined;
    @Schema(description = "커플 여부 (하위 호환용, coupleType 사용 권장)", example = "false")
    private boolean isCouple;
    @Schema(description = "커플 유형 (null=미등록, SOLO=혼자 둘러보기, CONNECTED=커플 연결 완료)", example = "CONNECTED")
    private String coupleType;
}
