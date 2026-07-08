package com.todaktodot.TDTD.domain.profile.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class SetOnboardingRequestDTO {

    @NotBlank(message = "닉네임을 입력해주세요")
    @Size(min = 1, max = 20, message = "닉네임은 1자 이상 20자 이하로 입력해주세요")
    private String nickname;

    @NotNull(message = "생년월일을 입력해주세요")
    private LocalDate birthDate;

    @NotBlank(message = "성별을 입력해주세요")
    private String gender;

    public SetOnboardingRequestDTO(String nickname, LocalDate birthDate, String gender) {
        this.nickname = nickname;
        this.birthDate = birthDate;
        this.gender = gender;
    }
}
