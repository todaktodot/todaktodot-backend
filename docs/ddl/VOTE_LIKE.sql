-- 투표 좋아요 테이블
CREATE TABLE vote_like (
    like_id         BIGINT          NOT NULL AUTO_INCREMENT COMMENT '좋아요 ID (PK)',
    vote_id         BIGINT          NOT NULL COMMENT '투표 ID (FK)',
    user_id         BIGINT          NOT NULL COMMENT '좋아요한 사용자 ID (FK)',
    reg_dt          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    regr_id         BIGINT          NOT NULL COMMENT '등록자 ID',
    upd_dt          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    updr_id         BIGINT          NOT NULL COMMENT '수정자 ID',
    del_yn          CHAR(1)         NOT NULL DEFAULT 'N' COMMENT '삭제 여부 (Y/N)',
    active_slot     INT             GENERATED ALWAYS AS (CASE WHEN del_yn = 'N' THEN 1 ELSE NULL END) VIRTUAL COMMENT '활성 row 유니크 제약용 슬롯',
    PRIMARY KEY (like_id),
    UNIQUE KEY uk_vote_user_active (vote_id, user_id, active_slot),
    INDEX idx_vote_id (vote_id, del_yn),
    INDEX idx_user_id (user_id, del_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='투표 좋아요';
