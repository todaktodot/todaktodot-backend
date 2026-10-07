-- 투표 AI 후보 테이블
CREATE TABLE vote_candidate (
    candidate_id    BIGINT          NOT NULL AUTO_INCREMENT COMMENT '후보 ID (PK)',
    batch_key       VARCHAR(20)     NOT NULL COMMENT '생성 묶음 키 (yyyy-MM-dd, 같은 날 생성분 묶음)',
    category        VARCHAR(20)     NOT NULL COMMENT '카테고리 (LOVE/ECONOMY/LIFESTYLE)',
    title           VARCHAR(100)    NOT NULL COMMENT '제목 - 어드민이 등록 전 수정 가능',
    options_json    VARCHAR(500)    NOT NULL COMMENT '선택지 JSON 배열 ["...","..."] - 2~5개, 각 20자 이내',
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT '처리 상태 (PENDING/APPROVED/REJECTED)',
    vote_id         BIGINT          NULL COMMENT '등록된 투표 ID - APPROVED 일 때만 값 존재',
    prompt_id       BIGINT          NULL COMMENT '생성에 사용한 AI_PROMPT ID',
    ai_model        VARCHAR(50)     NULL COMMENT '생성에 사용한 AI 모델명',
    reg_dt          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    regr_id         BIGINT          NOT NULL COMMENT '등록자 ID',
    upd_dt          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    updr_id         BIGINT          NOT NULL COMMENT '수정자 ID',
    del_yn          CHAR(1)         NOT NULL DEFAULT 'N' COMMENT '삭제 여부 (Y/N)',
    PRIMARY KEY (candidate_id),
    INDEX idx_status (status, del_yn, reg_dt),
    INDEX idx_batch_key (batch_key, del_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='투표 AI 후보';
