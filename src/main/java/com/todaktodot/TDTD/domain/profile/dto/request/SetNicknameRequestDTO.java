package com.todaktodot.TDTD.domain.profile.dto.request;

import com.todaktodot.TDTD.global.validation.Nickname;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SetNicknameRequestDTO {

    @Nickname
    private String nickname;

    public SetNicknameRequestDTO(String nickname) {
        this.nickname = nickname;
    }
}
