package com.todaktodot.TDTD.admin.prompt.repository.entity;

import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardSubject;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "SITUATION_CATEGORY")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SituationCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CATEGORY_ID")
    private Long categoryId;

    @Column(name = "SUBJECT", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private CardSubject subject;

    @Column(name = "CATEGORY_NAME", length = 50, nullable = false)
    private String categoryName;

    @Column(name = "CATEGORY_DESC", length = 200)
    private String categoryDesc;

    @Column(name = "SORT_ORDER", nullable = false)
    private Integer sortOrder = 0;

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
    public SituationCategoryEntity(CardSubject subject, String categoryName, String categoryDesc,
                                   Integer sortOrder, Long regrId, Long updrId) {
        this.subject = subject;
        this.categoryName = categoryName;
        this.categoryDesc = categoryDesc;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
        this.useYn = "Y";
        this.regrId = regrId;
        this.updrId = updrId;
        this.delYn = "N";
    }

    public void update(String categoryName, String categoryDesc, Integer sortOrder, Long updrId) {
        this.categoryName = categoryName;
        this.categoryDesc = categoryDesc;
        this.sortOrder = sortOrder;
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
