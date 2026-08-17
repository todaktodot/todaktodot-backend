-- 사용자 작성 정지 테이블
CREATE TABLE user_suspension (
    suspension_id   BIGINT          NOT NULL AUTO_INCREMENT COMMENT '정지 ID (PK)',
    user_id         BIGINT          NOT NULL COMMENT '대상 사용자 ID (FK)',
    status          VARCHAR(20)     NOT NULL DEFAULT 'SUSPENDED' COMMENT '정지 상태 (SUSPENDED, RELEASED)',
    suspended_dt    DATETIME        NOT NULL COMMENT '정지 일시',
    released_dt     DATETIME        NULL COMMENT '해제 일시',
    notice_ack_yn   CHAR(1)         NOT NULL DEFAULT 'N' COMMENT '정지 안내 팝업 확인 여부 (Y/N)',
    reg_dt          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    regr_id         BIGINT          NOT NULL COMMENT '등록자 ID',
    upd_dt          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    updr_id         BIGINT          NOT NULL COMMENT '수정자 ID',
    del_yn          CHAR(1)         NOT NULL DEFAULT 'N' COMMENT '삭제 여부 (Y/N)',
    active_slot     INT             GENERATED ALWAYS AS (CASE WHEN status = 'SUSPENDED' AND del_yn = 'N' THEN 1 ELSE NULL END) VIRTUAL COMMENT '활성 row 유니크 제약용 슬롯',
    PRIMARY KEY (suspension_id),
    UNIQUE KEY uk_user_active (user_id, active_slot),
    INDEX idx_user_status (user_id, status, del_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='사용자 작성 정지';
