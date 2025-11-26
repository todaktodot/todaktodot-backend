package com.todaktodot.TDTD.domain.login.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRequestDTO {
    //TODO: enum으로 변경
    private String provider;
    private String token;
}
