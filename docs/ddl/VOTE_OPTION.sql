-- 투표 답변 항목 테이블
CREATE TABLE vote_option (
    option_id       BIGINT          NOT NULL AUTO_INCREMENT COMMENT '답변 항목 ID (PK)',
    vote_id         BIGINT          NOT NULL COMMENT '투표 ID (FK)',
    sort_order      INT             NOT NULL COMMENT '표시 순서 (1~5)',
    content         VARCHAR(20)     NOT NULL COMMENT '답변 내용',
    reg_dt          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    regr_id         BIGINT          NOT NULL COMMENT '등록자 ID',
    upd_dt          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    updr_id         BIGINT          NOT NULL COMMENT '수정자 ID',
    del_yn          CHAR(1)         NOT NULL DEFAULT 'N' COMMENT '삭제 여부 (Y/N)',
    PRIMARY KEY (option_id),
    INDEX idx_vote_id (vote_id, del_yn, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='투표 답변 항목';
