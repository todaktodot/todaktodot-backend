package com.todaktodot.TDTD.domain.couplelink.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class IssueLinkCodeResponseDTO {
    private String linkCode;          // 발급된 코드
    private LocalDateTime expiredDt;  // 만료 시간

    public static IssueLinkCodeResponseDTO of(String linkCode, LocalDateTime expiredDt) {
        return IssueLinkCodeResponseDTO.builder()
                .linkCode(linkCode)
                .expiredDt(expiredDt)
                .build();
    }
}