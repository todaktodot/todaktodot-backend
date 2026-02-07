package com.todaktodot.TDTD.domain.notification.dto.reqeust;

import com.todaktodot.TDTD.domain.notification.repository.entity.DeviceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "디바이스 토큰 등록 요청")
public class DeviceTokenRequest {

    @NotBlank(message = "FCM 토큰은 필수입니다")
    @Schema(description = "FCM 토큰", example = "dK3xL9mN2pQ...")
    private String fcmToken;

    @NotNull(message = "디바이스 타입은 필수입니다")
    @Schema(description = "디바이스 타입", example = "IOS")
    private DeviceType deviceType;

    @Schema(description = "OS 버전", example = "iOS 17.2")
    private String osVersion;

    @Schema(description = "앱 버전", example = "1.0.0")
    private String appVersion;
}
