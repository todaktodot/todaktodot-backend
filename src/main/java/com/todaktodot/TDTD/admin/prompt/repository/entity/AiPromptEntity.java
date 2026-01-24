package com.todaktodot.TDTD.admin.prompt.repository.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "AI_PROMPT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiPromptEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PROMPT_ID")
    private Long promptId;

    @Column(name = "PROMPT_NAME", length = 100, nullable = false)
    private String promptName;

    @Column(name = "PROMPT_DESC", length = 500)
    private String promptDesc;

    @Column(name = "PROMPT_CONTENT", columnDefinition = "TEXT", nullable = false)
    private String promptContent;

    @Column(name = "VERSION", nullable = false)
    private Integer version = 1;

    @Column(name = "USE_YN", length = 1, columnDefinition = "CHAR(1) DEFAULT 'Y'")
    private String useYn = "Y";

    @CreationTimestamp
    @Column(name = "REG_DT", nullable = false, updatable = false)
    private LocalDateTime regDt;

    @Column(name = "REGR_ID", nullable = false)
    private Long regrId;

    @UpdateTimestamp
    @Column(name = "UPD_DT", nullable = false)
    private LocalDateTime updDt;

    @Column(name = "UPDR_ID", nullable = false)
    private Long updrId;

    @Column(name = "DEL_YN", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String delYn = "N";

    @Builder
    public AiPromptEntity(String promptName, String promptDesc, String promptContent,
                          Integer version, Long regrId, Long updrId) {
        this.promptName = promptName;
        this.promptDesc = promptDesc;
        this.promptContent = promptContent;
        this.version = version != null ? version : 1;
        this.useYn = "Y";
        this.regrId = regrId;
        this.updrId = updrId;
        this.delYn = "N";
    }

    public void update(String promptDesc, String promptContent, Long updrId) {
        this.promptDesc = promptDesc;
        this.promptContent = promptContent;
        this.updrId = updrId;
    }

    public void updateUseYn(String useYn, Long updrId) {
        this.useYn = useYn;
        this.updrId = updrId;
    }

    public boolean isActive() {
        return "Y".equals(useYn);
    }
}
