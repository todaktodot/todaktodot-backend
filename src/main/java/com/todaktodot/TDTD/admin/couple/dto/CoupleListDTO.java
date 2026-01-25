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
    private final String userName1;
    private final String userName2;
    private final LocalDate firstMetDt;
    private final String relationshipStage;
    private final LocalDateTime connectedDt;
    private final String delYn;
    private final LocalDateTime regDt;

    public CoupleListDTO(Long coupleId, Long userId1, Long userId2, String userName1, String userName2,
                         LocalDate firstMetDt, String relationshipStage, LocalDateTime connectedDt,
                         String delYn, LocalDateTime regDt) {
        this.coupleId = coupleId;
        this.userId1 = userId1;
        this.userId2 = userId2;
        this.userName1 = userName1;
        this.userName2 = userName2;
        this.firstMetDt = firstMetDt;
        this.relationshipStage = relationshipStage;
        this.connectedDt = connectedDt;
        this.delYn = delYn;
        this.regDt = regDt;
    }

    public String getUser1Display() {
        if (userId1 == null) {
            return "-";
        }
        if (userName1 == null || userName1.isBlank()) {
            return "-" + "(" + userId1 + ")";
        }
        return userName1 + "(" + userId1 + ")";
    }

    public String getUser2Display() {
        if (userId2 == null) {
            return "-";
        }
        if (userName2 == null || userName2.isBlank()) {
            return "-" + "(" + userId2 + ")";
        }
        return userName2 + "(" + userId2 + ")";
    }

    public static CoupleListDTO from(CoupleEntity entity, String userName1, String userName2) {
        return new CoupleListDTO(
                entity.getCoupleId(),
                entity.getUserId1(),
                entity.getUserId2(),
                userName1,
                userName2,
                entity.getFirstMetDt(),
                entity.getRelationshipStage() != null ? entity.getRelationshipStage().name() : "-",
                entity.getConnectedDt(),
                entity.getDelYn(),
                entity.getRegDt()
        );
    }
}
