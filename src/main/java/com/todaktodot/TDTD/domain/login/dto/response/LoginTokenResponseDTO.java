package com.todaktodot.TDTD.domain.login.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginTokenResponseDTO {
    private String accessToken;
    private String refreshToken;
}
