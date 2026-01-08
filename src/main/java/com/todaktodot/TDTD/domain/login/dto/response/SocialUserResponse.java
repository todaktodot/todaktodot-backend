package com.todaktodot.TDTD.domain.login.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class SocialUserResponse {
    private String id;
    private String email;
    private String name;
    private String provider;
}
