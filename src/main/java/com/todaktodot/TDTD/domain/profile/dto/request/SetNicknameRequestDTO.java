package com.todaktodot.TDTD.domain.profile.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SetNicknameRequestDTO {

    @NotBlank(message = "닉네임을 입력해주세요")
    @Size(min = 1, max = 20, message = "닉네임은 1자 이상 20자 이하로 입력해주세요")
    private String nickname;

    public SetNicknameRequestDTO(String nickname) {
        this.nickname = nickname;
    }
}
