package com.todaktodot.TDTD.domain.couple.dto.response;

import com.todaktodot.TDTD.domain.couple.repository.entity.CoupleEntity;
import com.todaktodot.TDTD.domain.couple.repository.entity.CoupleType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SoloStartResponseDTO {

    private Long coupleId;
    private CoupleType coupleType;
    private LocalDateTime startedAt;
    private String message;

    public static SoloStartResponseDTO from(CoupleEntity entity) {
        return SoloStartResponseDTO.builder()
                .coupleId(entity.getCoupleId())
                .coupleType(entity.getCoupleType())
                .startedAt(entity.getConnectedDt())
                .message("혼자 둘러보기가 시작되었습니다")
                .build();
    }
}
