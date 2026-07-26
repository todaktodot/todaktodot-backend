package com.todaktodot.TDTD.admin.couple.dto;

import com.todaktodot.TDTD.domain.login.respository.entity.User;
import java.time.LocalDateTime;

import com.todaktodot.TDTD.domain.login.respository.entity.UserAccount;
import lombok.Getter;

@Getter
public class UserSummaryDTO {

    private final Long userId;
    private final String name;
    private final String nickname;
    private final String kakaoNickname;
    private final String email;
    private final String provider;
    private final String joinYn;
    private final LocalDateTime regDt;

    public UserSummaryDTO(Long userId, String name, String nickname, String kakaoNickname, String email,
                          String provider, String joinYn, LocalDateTime regDt) {
        this.userId = userId;
        this.name = name;
        this.nickname = nickname;
        this.kakaoNickname = kakaoNickname;
        this.email = email;
        this.provider = provider;
        this.joinYn = joinYn;
        this.regDt = regDt;
    }

    public static UserSummaryDTO from(User user) {
        UserAccount userAccount = user.getSocialAccounts().getFirst();
        return new UserSummaryDTO(
                user.getId(),
                userAccount.getName(),
                user.getNickname(),
                userAccount.getKakaoNickname(),
                userAccount.getEmail(),
                userAccount.getProvider(),
                user.getTermYN(),
                user.getRegDt()
        );
    }
}
