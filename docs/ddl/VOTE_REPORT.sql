-- 투표 신고 테이블
CREATE TABLE vote_report (
    report_id       BIGINT          NOT NULL AUTO_INCREMENT COMMENT '신고 ID (PK)',
    vote_id         BIGINT          NOT NULL COMMENT '신고 대상 투표 ID (FK)',
    user_id         BIGINT          NOT NULL COMMENT '신고자 사용자 ID (FK)',
    reason          VARCHAR(20)     NOT NULL COMMENT '신고 사유 (ABUSE, OBSCENE, SPAM, ADVERTISEMENT, PRIVACY, ILLEGAL, DISLIKE)',
    reg_dt          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    regr_id         BIGINT          NOT NULL COMMENT '등록자 ID',
    upd_dt          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    updr_id         BIGINT          NOT NULL COMMENT '수정자 ID',
    del_yn          CHAR(1)         NOT NULL DEFAULT 'N' COMMENT '삭제 여부 (Y/N)',
    PRIMARY KEY (report_id),
    UNIQUE KEY uk_vote_user (vote_id, user_id),
    INDEX idx_user_id (user_id),
    INDEX idx_vote_id (vote_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='투표 신고';
