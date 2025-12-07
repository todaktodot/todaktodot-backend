package com.todaktodot.TDTD.domain.couplelink.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ConnectLinkCodeRequestDTO {

    @NotBlank(message = "사용자 ID는 필수입니다")
    private String userId;  // 코드를 입력하는 사용자

    @NotBlank(message = "링크 코드는 필수입니다")
    @Pattern(regexp = "^[A-Z0-9]{6}$", message = "링크 코드는 6자리 영문 대문자 및 숫자 조합이어야 합니다")
    private String linkCode;  // 커플 연결 코드
}