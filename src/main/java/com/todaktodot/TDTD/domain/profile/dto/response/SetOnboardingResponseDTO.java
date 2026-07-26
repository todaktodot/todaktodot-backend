package com.todaktodot.TDTD.domain.profile.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class SetOnboardingResponseDTO {

    private Long userId;
    private String nickname;
    private String birthDate;
    private String gender;
    private String message;

    public static SetOnboardingResponseDTO of(Long userId, String nickname, LocalDate birthDate, String gender) {
        return SetOnboardingResponseDTO.builder()
                .userId(userId)
                .nickname(nickname)
                .birthDate(birthDate.toString())
                .gender(gender)
                .message("온보딩 정보가 설정되었습니다")
                .build();
    }
}