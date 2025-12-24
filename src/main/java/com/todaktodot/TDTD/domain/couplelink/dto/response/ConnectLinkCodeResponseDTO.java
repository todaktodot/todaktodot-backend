package com.todaktodot.TDTD.domain.couplelink.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ConnectLinkCodeResponseDTO {

    private Long coupleId;           // 커플 ID
    private Long userId1;            // 사용자 1 (코드 발급자)
    private Long userId2;            // 사용자 2 (코드 입력자)
    private LocalDateTime connectedDt;  // 커플 연결 일자

    public static ConnectLinkCodeResponseDTO of(Long coupleId, Long userId1, Long userId2, LocalDateTime connectedDt) {
        return ConnectLinkCodeResponseDTO.builder()
                .coupleId(coupleId)
                .userId1(userId1)
                .userId2(userId2)
                .connectedDt(connectedDt)
                .build();
    }
}