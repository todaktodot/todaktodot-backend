package com.todaktodot.TDTD.admin.prompt.dto;

import com.todaktodot.TDTD.admin.prompt.repository.entity.AiPromptEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AiPromptDTO {

    private Long promptId;
    private Long promptGroupId;
    private String promptName;
    private String promptType;
    private String promptTypeDisplayName;
    private String promptDesc;
    private String promptContent;
    private Integer version;
    private String useYn;
    private LocalDateTime regDt;
    private LocalDateTime updDt;

    public static AiPromptDTO from(AiPromptEntity entity) {
        return AiPromptDTO.builder()
                .promptId(entity.getPromptId())
                .promptGroupId(entity.getPromptGroupId())
                .promptName(entity.getPromptName())
                .promptType(entity.getPromptType().name())
                .promptTypeDisplayName(entity.getPromptType().getDisplayName())
                .promptDesc(entity.getPromptDesc())
                .promptContent(entity.getPromptContent())
                .version(entity.getVersion())
                .useYn(entity.getUseYn())
                .regDt(entity.getRegDt())
                .updDt(entity.getUpdDt())
                .build();
    }

    public boolean isActive() {
        return "Y".equals(useYn);
    }

    @Getter
    @Setter
    public static class CreateRequest {
        private String promptName;
        private String promptType;
        private String promptDesc;
        private String promptContent;
    }

    @Getter
    @Setter
    public static class UpdateRequest {
        private String promptDesc;
        private String promptContent;
    }
}
