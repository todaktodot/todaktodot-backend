package com.todaktodot.TDTD.admin.couple.dto;

import com.todaktodot.TDTD.domain.couple.repository.entity.CoupleEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class CoupleListDTO {

    private final Long coupleId;
    private final Long userId1;
    private final Long userId2;
    private final LocalDate firstMetDt;
    private final String relationshipStage;
    private final LocalDateTime connectedDt;
    private final String delYn;
    private final LocalDateTime regDt;

    public CoupleListDTO(Long coupleId, Long userId1, Long userId2, LocalDate firstMetDt,
                         String relationshipStage, LocalDateTime connectedDt, String delYn, LocalDateTime regDt) {
        this.coupleId = coupleId;
        this.userId1 = userId1;
        this.userId2 = userId2;
        this.firstMetDt = firstMetDt;
        this.relationshipStage = relationshipStage;
        this.connectedDt = connectedDt;
        this.delYn = delYn;
        this.regDt = regDt;
    }

    public static CoupleListDTO from(CoupleEntity entity) {
        return new CoupleListDTO(
                entity.getCoupleId(),
                entity.getUserId1(),
                entity.getUserId2(),
                entity.getFirstMetDt(),
                entity.getRelationshipStage() != null ? entity.getRelationshipStage().name() : "-",
                entity.getConnectedDt(),
                entity.getDelYn(),
                entity.getRegDt()
        );
    }
}
