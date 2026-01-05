package com.todaktodot.TDTD.admin.prompt.dto;

import com.todaktodot.TDTD.admin.prompt.repository.entity.SituationCategoryEntity;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardSubject;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SituationCategoryDTO {

    private Long categoryId;
    private CardSubject subject;
    private String categoryName;
    private String categoryDesc;
    private Integer sortOrder;
    private String useYn;
    private LocalDateTime regDt;
    private LocalDateTime updDt;

    public static SituationCategoryDTO from(SituationCategoryEntity entity) {
        return SituationCategoryDTO.builder()
                .categoryId(entity.getCategoryId())
                .subject(entity.getSubject())
                .categoryName(entity.getCategoryName())
                .categoryDesc(entity.getCategoryDesc())
                .sortOrder(entity.getSortOrder())
                .useYn(entity.getUseYn())
                .regDt(entity.getRegDt())
                .updDt(entity.getUpdDt())
                .build();
    }

    public String getSubjectDisplayName() {
        return subject != null ? subject.getDisplayName() : "";
    }

    public boolean isActive() {
        return "Y".equals(useYn);
    }

    @Getter
    @Setter
    public static class CreateRequest {
        private CardSubject subject;
        private String categoryName;
        private String categoryDesc;
        private Integer sortOrder;
    }

    @Getter
    @Setter
    public static class UpdateRequest {
        private String categoryName;
        private String categoryDesc;
        private Integer sortOrder;
    }
}
