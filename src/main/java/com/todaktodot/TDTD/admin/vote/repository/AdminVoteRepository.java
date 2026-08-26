package com.todaktodot.TDTD.admin.vote.repository;

import com.todaktodot.TDTD.admin.vote.dto.AdminVoteListDTO;
import com.todaktodot.TDTD.admin.vote.dto.AdminVoteSearchCondition;
import com.todaktodot.TDTD.admin.vote.dto.AdminVoteStatsDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 어드민 투표 목록 조회. 신고건수/좋아요수/대기경과 등 집계 값이 필터·정렬 대상이라
 * CTE(WITH) 로 계산 컬럼을 만든 뒤 그 위에서 필터링한다.
 */
@Repository
@RequiredArgsConstructor
public class AdminVoteRepository {

    @PersistenceContext
    private final EntityManager em;

    private static final Map<String, String> VOTE_STATUS_LABEL = Map.of(
            "ACTIVE", "진행중",
            "CLOSED", "마감",
            "AUTO_HIDDEN", "자동숨김",
            "HIDDEN", "숨김"
    );

    private static final Map<String, String> REPORT_STATUS_LABEL = Map.of(
            "NONE", "신고 없음",
            "PENDING", "검토 필요",
            "RESOLVED", "검토 완료"
    );

    private static final String CALC_CTE = """
            WITH v_calc AS (
                SELECT
                    v.VOTE_ID AS vote_id,
                    v.TITLE AS title,
                    v.CATEGORY AS category,
                    v.RANDOM_NICKNAME AS random_nickname,
                    v.USER_ID AS user_id,
                    v.PARTICIPANT_CNT AS participant_cnt,
                    v.REG_DT AS reg_dt,
                    (SELECT COUNT(*) FROM vote_like l WHERE l.VOTE_ID = v.VOTE_ID AND l.DEL_YN = 'N') AS like_cnt,
                    (SELECT COUNT(*) FROM vote_report r
                        WHERE r.VOTE_ID = v.VOTE_ID AND r.DEL_YN = 'N'
                        AND (v.REVIEW_CYCLE_STARTED_AT IS NULL OR r.REG_DT >= v.REVIEW_CYCLE_STARTED_AT)) AS report_cnt,
                    (SELECT MIN(r.REG_DT) FROM vote_report r
                        WHERE r.VOTE_ID = v.VOTE_ID AND r.DEL_YN = 'N'
                        AND (v.REVIEW_CYCLE_STARTED_AT IS NULL OR r.REG_DT >= v.REVIEW_CYCLE_STARTED_AT)) AS wait_start,
                    CASE
                        WHEN v.STATUS = 'HIDDEN' AND v.HIDE_REASON = 'AUTO' THEN 'AUTO_HIDDEN'
                        WHEN v.STATUS = 'HIDDEN' THEN 'HIDDEN'
                        WHEN v.CLOSED_AT > NOW() THEN 'ACTIVE'
                        ELSE 'CLOSED'
                    END AS vote_status_code
                FROM vote v
                WHERE v.DEL_YN = 'N'
            )
            """;

    // count 전용
    private static final String COUNT_CALC_CTE = """
            WITH v_calc AS (
                SELECT
                    v.VOTE_ID AS vote_id,
                    v.TITLE AS title,
                    v.CATEGORY AS category,
                    v.RANDOM_NICKNAME AS random_nickname,
                    v.USER_ID AS user_id,
                    v.REG_DT AS reg_dt,
                    (SELECT COUNT(*) FROM vote_report r
                        WHERE r.VOTE_ID = v.VOTE_ID AND r.DEL_YN = 'N'
                        AND (v.REVIEW_CYCLE_STARTED_AT IS NULL OR r.REG_DT >= v.REVIEW_CYCLE_STARTED_AT)) AS report_cnt,
                    CASE
                        WHEN v.STATUS = 'HIDDEN' AND v.HIDE_REASON = 'AUTO' THEN 'AUTO_HIDDEN'
                        WHEN v.STATUS = 'HIDDEN' THEN 'HIDDEN'
                        WHEN v.CLOSED_AT > NOW() THEN 'ACTIVE'
                        ELSE 'CLOSED'
                    END AS vote_status_code
                FROM vote v
                WHERE v.DEL_YN = 'N'
            )
            """;

    private static final String REPORT_STATUS_CASE = """
            CASE
                WHEN report_cnt = 0 THEN 'NONE'
                WHEN vote_status_code = 'HIDDEN' THEN 'RESOLVED'
                ELSE 'PENDING'
            END
            """;

    public Page<AdminVoteListDTO> findList(AdminVoteSearchCondition cond, Pageable pageable) {
        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        if (cond.hasCategory()) {
            where.append(" AND category = :category ");
        }
        if (cond.hasVoteStatus()) {
            where.append(" AND vote_status_code = :voteStatus ");
        }
        if (cond.hasReportStatus()) {
            where.append(" AND (").append(REPORT_STATUS_CASE).append(") = :reportStatus ");
        }
        if (cond.getStartDt() != null) {
            where.append(" AND reg_dt >= :startDt ");
        }
        if (cond.getEndDt() != null) {
            where.append(" AND reg_dt < :endDt ");
        }
        if (cond.hasKeyword()) {
            where.append(" AND (title LIKE :keyword OR random_nickname LIKE :keyword OR CAST(user_id AS CHAR) = :userIdKeyword) ");
        }

        String orderBy = switch (cond.getSort() == null ? "LATEST" : cond.getSort()) {
            case "REPORT_DESC" -> " ORDER BY report_cnt DESC, reg_dt DESC ";
            case "WAIT_ASC" -> " ORDER BY (wait_start IS NULL) ASC, wait_start ASC ";
            default -> " ORDER BY reg_dt DESC ";
        };

        String selectSql = CALC_CTE + """
                SELECT vote_id, title, category, random_nickname, participant_cnt, like_cnt, report_cnt,
                       vote_status_code, wait_start, reg_dt
                FROM v_calc
                """ + where + orderBy + " LIMIT :limit OFFSET :offset ";

        String countSql = COUNT_CALC_CTE + " SELECT COUNT(*) FROM v_calc " + where;

        Query selectQuery = em.createNativeQuery(selectSql);
        Query countQuery = em.createNativeQuery(countSql);
        bindParams(selectQuery, cond);
        bindParams(countQuery, cond);
        selectQuery.setParameter("limit", pageable.getPageSize());
        selectQuery.setParameter("offset", pageable.getOffset());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = selectQuery.getResultList();
        long total = ((Number) countQuery.getSingleResult()).longValue();

        List<AdminVoteListDTO> content = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (Object[] row : rows) {
            String voteStatusCode = (String) row[7];
            long reportCnt = ((Number) row[6]).longValue();
            String reportStatusCode = reportCnt == 0 ? "NONE"
                    : "HIDDEN".equals(voteStatusCode) ? "RESOLVED" : "PENDING";

            Timestamp waitStartTs = (Timestamp) row[8];
            String waitingDisplay = null;
            boolean waitingOverdue = false;
            if ("PENDING".equals(reportStatusCode) && waitStartTs != null) {
                LocalDateTime waitStart = waitStartTs.toLocalDateTime();
                long totalMinutes = java.time.Duration.between(waitStart, now).toMinutes();
                long days = totalMinutes / (60 * 24);
                long hours = (totalMinutes % (60 * 24)) / 60;
                waitingDisplay = days + "일 " + String.format("%02d", hours) + "시간";
                waitingOverdue = days > 3;
            }

            content.add(new AdminVoteListDTO(
                    ((Number) row[0]).longValue(),
                    (String) row[1],
                    labelOfCategory((String) row[2]),
                    (String) row[3],
                    ((Number) row[4]).intValue(),
                    ((Number) row[5]).longValue(),
                    reportCnt,
                    voteStatusCode,
                    VOTE_STATUS_LABEL.get(voteStatusCode),
                    reportStatusCode,
                    REPORT_STATUS_LABEL.get(reportStatusCode),
                    waitingDisplay,
                    waitingOverdue,
                    ((Timestamp) row[9]).toLocalDateTime()
            ));
        }

        return new PageImpl<>(content, pageable, total);
    }

    public AdminVoteStatsDTO getStats() {
        String sql = COUNT_CALC_CTE + """
                SELECT
                    COUNT(*),
                    SUM(CASE WHEN vote_status_code = 'ACTIVE' THEN 1 ELSE 0 END),
                    SUM(CASE WHEN vote_status_code = 'CLOSED' THEN 1 ELSE 0 END),
                    SUM(CASE WHEN vote_status_code IN ('AUTO_HIDDEN', 'HIDDEN') THEN 1 ELSE 0 END),
                    SUM(CASE WHEN report_cnt > 0 AND vote_status_code != 'HIDDEN' THEN 1 ELSE 0 END)
                FROM v_calc
                """;
        Object[] row = (Object[]) em.createNativeQuery(sql).getSingleResult();
        return new AdminVoteStatsDTO(
                ((Number) row[0]).longValue(),
                nvl(row[1]), nvl(row[2]), nvl(row[3]), nvl(row[4])
        );
    }

    private long nvl(Object o) {
        return o == null ? 0L : ((Number) o).longValue();
    }

    private void bindParams(Query query, AdminVoteSearchCondition cond) {
        if (cond.hasCategory()) {
            query.setParameter("category", cond.getCategory());
        }
        if (cond.hasVoteStatus()) {
            query.setParameter("voteStatus", cond.getVoteStatus());
        }
        if (cond.hasReportStatus()) {
            query.setParameter("reportStatus", cond.getReportStatus());
        }
        if (cond.getStartDt() != null) {
            query.setParameter("startDt", cond.getStartDt().atStartOfDay());
        }
        if (cond.getEndDt() != null) {
            query.setParameter("endDt", cond.getEndDt().plusDays(1).atStartOfDay());
        }
        if (cond.hasKeyword()) {
            query.setParameter("keyword", "%" + cond.getKeyword() + "%");
            query.setParameter("userIdKeyword", cond.getKeyword());
        }
    }

    private String labelOfCategory(String category) {
        return switch (category) {
            case "LOVE" -> "연애관";
            case "ECONOMY" -> "경제관";
            case "LIFESTYLE" -> "생활관";
            default -> category;
        };
    }
}
