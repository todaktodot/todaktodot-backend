package com.todaktodot.TDTD.domain.couple.dto.response;

import com.todaktodot.TDTD.domain.couple.repository.entity.CoupleEntity;
import com.todaktodot.TDTD.domain.couple.repository.entity.RelationshipStage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class CoupleInfoResponseDTO {

    private Long coupleId;
    private Long userId1;
    private Long userId2;
    private LocalDateTime connectedDt;
    private LocalDate firstMetDt;
    private RelationshipStage relationshipStage;

    public static CoupleInfoResponseDTO from(CoupleEntity entity) {
        return CoupleInfoResponseDTO.builder()
                .coupleId(entity.getCoupleId())
                .userId1(entity.getUserId1())
                .userId2(entity.getUserId2())
                .connectedDt(entity.getConnectedDt())
                .firstMetDt(entity.getFirstMetDt())
                .relationshipStage(entity.getRelationshipStage())
                .build();
    }
}
