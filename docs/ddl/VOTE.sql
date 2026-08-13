-- 투표 글
CREATE TABLE VOTE (
    VOTE_ID                 BIGINT          NOT NULL AUTO_INCREMENT COMMENT '투표 ID (PK)',
    USER_ID                 BIGINT          NOT NULL COMMENT '작성자 ID (화면 비노출, 신고/정지 처리용)',
    RANDOM_NICKNAME         VARCHAR(30)     NOT NULL COMMENT '랜덤 조합 닉네임 (작성 시 1회 생성 후 불변)',
    CATEGORY                VARCHAR(20)     NOT NULL COMMENT '카테고리 (LOVE/ECONOMY/LIFESTYLE)',
    TITLE                   VARCHAR(100)    NOT NULL COMMENT '제목',
    STATUS                  VARCHAR(20)     NOT NULL DEFAULT 'POSTED' COMMENT '노출 상태 (POSTED/HIDDEN)',
    HIDE_REASON             VARCHAR(20)     NULL COMMENT '숨김 사유 (AUTO/ADMIN), HIDDEN일 때만 값 존재',
    CLOSED_AT               DATETIME        NOT NULL COMMENT '마감 시각 (등록+24h, 수정 불변)',
    PARTICIPANT_CNT         INT             NOT NULL DEFAULT 0 COMMENT '총 참여 수',
    AUTO_HIDE_EXEMPT_YN     CHAR(1)         NOT NULL DEFAULT 'N' COMMENT '자동 숨김 예외 플래그',
    HIDDEN_NOTICE_ACK_YN    CHAR(1)         NOT NULL DEFAULT 'N' COMMENT '숨김 안내 팝업 확인 여부',
    REG_DT                  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    REGR_ID                 BIGINT          NOT NULL COMMENT '등록자 ID',
    UPD_DT                  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    UPDR_ID                 BIGINT          NOT NULL COMMENT '수정자 ID',
    DEL_YN                  CHAR(1)         NOT NULL DEFAULT 'N' COMMENT '삭제 여부 (Y/N)',
    PRIMARY KEY (VOTE_ID),
    INDEX IDX_FEED_LATEST (DEL_YN, STATUS, REG_DT, VOTE_ID),
    INDEX IDX_FEED_POPULAR (DEL_YN, STATUS, PARTICIPANT_CNT, VOTE_ID),
    INDEX IDX_AUTHOR (USER_ID, DEL_YN, REG_DT),
    INDEX IDX_CATEGORY (CATEGORY, DEL_YN, STATUS)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='투표 글';
