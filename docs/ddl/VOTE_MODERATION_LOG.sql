-- 투표 상태 변경 이력 (어드민 상세 모달 "상태 변경 이력" 표시용)
CREATE TABLE vote_moderation_log (
    LOG_ID          BIGINT          NOT NULL AUTO_INCREMENT COMMENT '로그 ID (PK)',
    VOTE_ID         BIGINT          NOT NULL COMMENT '투표 ID',
    PREV_STATUS     VARCHAR(20)     NULL COMMENT '변경 전 상태 (최초 등록 시 NULL)',
    NEW_STATUS      VARCHAR(20)     NOT NULL COMMENT '변경 후 상태',
    ACTOR           VARCHAR(50)     NOT NULL COMMENT '처리자 - system(자동) 또는 관리자 계정명',
    MEMO            VARCHAR(200)    NULL COMMENT '변경 사유 (예: 신고 10건 도달, 관리자 수동 숨김, 관리자 반려)',
    REG_DT          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    REGR_ID         BIGINT          NOT NULL COMMENT '등록자 ID',
    UPD_DT          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    UPDR_ID         BIGINT          NOT NULL COMMENT '수정자 ID',
    DEL_YN          CHAR(1)         NOT NULL DEFAULT 'N' COMMENT '삭제 여부 (Y/N)',
    PRIMARY KEY (LOG_ID),
    INDEX IDX_VOTE_ID (VOTE_ID, REG_DT)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='투표 상태 변경 이력';
