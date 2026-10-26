-- 투표 초안(DRAFT) 추가에 따른 VOTE 변경
-- VOTE.sql 은 신규 생성용이고, 이미 테이블이 있는 dev / prod 에는 이 스크립트를 실행한다.

-- 마감 시각 NULL 허용 - 초안은 게시 전부터 마감 시계가 돌면 안 되므로 게시 시점에 채운다
ALTER TABLE vote
    MODIFY COLUMN closed_at DATETIME NULL
    COMMENT '마감 시각 (게시+24h, 수정 불변). DRAFT는 아직 마감 시계가 돌면 안 되므로 NULL, 게시 시점에 채움';

-- 상태 컬럼 주석에 DRAFT 반영 (값은 애플리케이션이 관리)
ALTER TABLE vote
    MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'POSTED'
    COMMENT '노출 상태 (DRAFT/POSTED/HIDDEN) - DRAFT는 게시 전 초안이라 피드/마이페이지/상세에 노출하지 않음';

-- 확인 : 기존 행은 전부 POSTED/HIDDEN 이고 closed_at 이 채워져 있어야 한다
-- SELECT status, COUNT(*), SUM(closed_at IS NULL) AS closed_at_null FROM vote GROUP BY status;
