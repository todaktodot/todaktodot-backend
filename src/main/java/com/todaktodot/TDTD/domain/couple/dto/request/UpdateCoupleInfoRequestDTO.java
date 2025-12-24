package com.todaktodot.TDTD.domain.couple.dto.request;

import com.todaktodot.TDTD.domain.couple.repository.entity.RelationshipStage;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCoupleInfoRequestDTO {

    @NotNull(message = "우리가 만난 날은 필수입니다")
    private LocalDate firstMetDt;

    @NotNull(message = "관계 단계는 필수입니다")
    private RelationshipStage relationshipStage;
}
