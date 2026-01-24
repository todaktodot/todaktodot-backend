package com.todaktodot.TDTD.admin.couple.dto;

import com.todaktodot.TDTD.domain.login.respository.entity.User;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class UserSummaryDTO {

    private final Long userId;
    private final String name;
    private final String nickname;
    private final String email;
    private final String provider;
    private final String joinYn;
    private final LocalDateTime regDt;

    public UserSummaryDTO(Long userId, String name, String nickname, String email,
                          String provider, String joinYn, LocalDateTime regDt) {
        this.userId = userId;
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.provider = provider;
        this.joinYn = joinYn;
        this.regDt = regDt;
    }

    public static UserSummaryDTO from(User user) {
        return new UserSummaryDTO(
                user.getId(),
                user.getName(),
                user.getNickname(),
                user.getEmail(),
                user.getProvider(),
                user.getJoinYN(),
                user.getRegDt()
        );
    }
}
