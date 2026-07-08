package com.todaktodot.TDTD.domain.profile.dto.response;

import com.todaktodot.TDTD.domain.login.respository.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 회원정보
 */
@Getter
@Setter
@Builder
public class UserDetailResponseDTO {
    @Schema(description = "userID")
    private Long userId;
    @Schema(description = "닉네임")
    private String nickname;
    @Schema(description = "생년월일")
    private String birthDate;
    @Schema(description = "성별")
    private String gender;
    @Schema(description = "역할")
    private String role;
    @Schema(description = "정보성 알림 동의 여부")
    private String infoAlarmYN;
    @Schema(description = "광고성 알림 동의 여부")
    private String adAlarmYN;
    @Schema(description = "마케팅 동의 여부")
    private String marketingAlarmYN;
    @Schema(description = "약관 동의 여부")
    private String isTerm;
    @Schema(description = "커플 연결 여부")
    private String isCouple;
    @Schema(description = "커플 유형 (null=미등록, SOLO=혼자 둘러보기, CONNECTED=커플 연결 완료)", example = "CONNECTED")
    private String coupleType;
    @Schema(description = "삭제 여부")
    private String delYn;
    @Schema(description = "커플 정보")
    private CoupleDetail coupleDetailInfo;

    public static UserDetailResponseDTO of(User user) {
        return UserDetailResponseDTO.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .birthDate(user.getBirthDate().toString())
                .gender(user.getGender().getCode())
                .role(user.getRole().getRole())
                .infoAlarmYN(user.getInfoAlarmYN())
                .adAlarmYN(user.getAdAlarmYN())
                .marketingAlarmYN(user.getMarketingAlarmYN())
                .isTerm(user.getTermYN())
                .delYn(user.getDelYn())
                .build();
    }

    @Getter
    @Builder
    public static class CoupleDetail {
        @Schema(description = "coupleID")
        private Long coupleId;
        @Schema(description = "로그인한 userId")
        private Long loginUserId;
        @Schema(description = "로그인한 회원 닉네임")
        private String loginNickname;
        @Schema(description = "다른 회원 userId")
        private Long anotherUserId;
        @Schema(description = "다른 회원 닉네임")
        private String anotherNickname;
        @Schema(description = "처음 만난 날")
        private LocalDate firstMetDt;
        @Schema(description = "우리가 만난지")
        private String sinceMetDt;
        @Schema(description = "우리의 관계")
        private String relationshipStage;
        @Schema(description = "커플 연결한 날짜")
        private LocalDateTime connectedDt;
        @Schema(description = "커플 삭제 역부")
        private String delYn;
    }
}