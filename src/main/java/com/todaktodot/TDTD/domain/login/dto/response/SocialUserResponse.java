package com.todaktodot.TDTD.domain.login.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class SocialUserResponse {
    private String id;
    private String email;
    private String name;
    private String provider;
}
