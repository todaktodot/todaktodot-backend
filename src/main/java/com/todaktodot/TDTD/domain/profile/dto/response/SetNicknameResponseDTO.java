package com.todaktodot.TDTD.domain.profile.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SetNicknameResponseDTO {

    private Long userId;
    private String nickname;
    private String message;

    public static SetNicknameResponseDTO of(Long userId, String nickname) {
        return SetNicknameResponseDTO.builder()
                .userId(userId)
                .nickname(nickname)
                .message("닉네임이 설정되었습니다")
                .build();
    }
}