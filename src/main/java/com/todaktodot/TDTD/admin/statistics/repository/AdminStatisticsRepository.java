package com.todaktodot.TDTD.admin.statistics.repository;

import com.todaktodot.TDTD.admin.statistics.repository.projection.WeeklyStatisticsProjection;
import com.todaktodot.TDTD.domain.login.respository.entity.User;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface AdminStatisticsRepository extends Repository<User, Long> {

    @Query(nativeQuery = true, value = """
            WITH RECURSIVE weekly_periods AS (
                SELECT CAST(:startDate AS DATE) AS period_start_date,
                       LEAST(
                           DATE_ADD(CAST(:startDate AS DATE), INTERVAL MOD(1 - WEEKDAY(CAST(:startDate AS DATE)) + 7, 7) DAY),
                           CAST(:endDate AS DATE)
                       ) AS period_end_date
                UNION ALL
                SELECT DATE_ADD(period_end_date, INTERVAL 1 DAY) AS period_start_date,
                       LEAST(DATE_ADD(period_end_date, INTERVAL 7 DAY), CAST(:endDate AS DATE)) AS period_end_date
                  FROM weekly_periods
                 WHERE period_end_date < CAST(:endDate AS DATE)
            )
            SELECT stats.period_start_date AS periodStartDate,
                   stats.period_end_date AS periodEndDate,
                   stats.total_user_count AS totalUserCount,
                   stats.total_couple_count AS totalCoupleCount,
                   stats.selected_card_count AS dailyCardCount,
                   stats.answered_user_count AS answeredUserCount,
                   COALESCE(ROUND(stats.answer_event_count / NULLIF(stats.answer_opportunity_count, 0) * 100, 2), 0) AS personalAnswerRate,
                   stats.selected_card_count AS historyCardCount,
                   stats.ai_feedback_count AS aiFeedbackCount,
                   COALESCE(ROUND(stats.both_answered_card_count / NULLIF(stats.selected_card_count, 0) * 100, 2), 0) AS coupleBothAnswerRate
              FROM (
                    SELECT wp.period_start_date,
                           wp.period_end_date,
                           (
                               SELECT COUNT(*)
                                 FROM users u
                                WHERE u.del_yn = 'N'
                                  AND u.reg_dt < DATE_ADD(wp.period_end_date, INTERVAL 1 DAY)
                           ) AS total_user_count,
                           (
                               SELECT COUNT(*)
                                 FROM couple c
                                WHERE c.del_yn = 'N'
                                  AND c.couple_type = 'CONNECTED'
                                  AND c.user_id_2 IS NOT NULL
                                  AND c.connected_dt < DATE_ADD(wp.period_end_date, INTERVAL 1 DAY)
                           ) AS total_couple_count,
                           (
                               SELECT COUNT(*)
                                 FROM couple_daily_card cdc
                                 JOIN couple c
                                   ON c.couple_id = cdc.couple_id
                                  AND c.del_yn = 'N'
                                  AND c.couple_type = 'CONNECTED'
                                  AND c.user_id_2 IS NOT NULL
                                WHERE cdc.del_yn = 'N'
                                  AND cdc.selected_yn = 'Y'
                                  AND cdc.issued_date BETWEEN wp.period_start_date AND wp.period_end_date
                           ) AS selected_card_count,
                           (
                               SELECT COUNT(DISTINCT dcua.user_id)
                                 FROM daily_card_user_answer dcua
                                 JOIN couple_daily_card cdc
                                   ON cdc.couple_card_id = dcua.couple_card_id
                                  AND cdc.del_yn = 'N'
                                  AND cdc.selected_yn = 'Y'
                                  AND cdc.issued_date BETWEEN wp.period_start_date AND wp.period_end_date
                                 JOIN couple c
                                   ON c.couple_id = cdc.couple_id
                                  AND c.del_yn = 'N'
                                  AND c.couple_type = 'CONNECTED'
                                  AND c.user_id_2 IS NOT NULL
                                WHERE dcua.del_yn = 'N'
                           ) AS answered_user_count,
                           (
                               SELECT COUNT(DISTINCT CONCAT(dcua.couple_card_id, ':', dcua.user_id))
                                 FROM daily_card_user_answer dcua
                                 JOIN couple_daily_card cdc
                                   ON cdc.couple_card_id = dcua.couple_card_id
                                  AND cdc.del_yn = 'N'
                                  AND cdc.selected_yn = 'Y'
                                  AND cdc.issued_date BETWEEN wp.period_start_date AND wp.period_end_date
                                 JOIN couple c
                                   ON c.couple_id = cdc.couple_id
                                  AND c.del_yn = 'N'
                                  AND c.couple_type = 'CONNECTED'
                                  AND c.user_id_2 IS NOT NULL
                                WHERE dcua.del_yn = 'N'
                           ) AS answer_event_count,
                           (
                               SELECT COUNT(*) * 2
                                 FROM couple_daily_card cdc
                                 JOIN couple c
                                   ON c.couple_id = cdc.couple_id
                                  AND c.del_yn = 'N'
                                  AND c.couple_type = 'CONNECTED'
                                  AND c.user_id_2 IS NOT NULL
                                WHERE cdc.del_yn = 'N'
                                  AND cdc.selected_yn = 'Y'
                                  AND cdc.issued_date BETWEEN wp.period_start_date AND wp.period_end_date
                           ) AS answer_opportunity_count,
                           (
                               SELECT COUNT(*)
                                 FROM couple_daily_card cdc
                                 JOIN couple c
                                   ON c.couple_id = cdc.couple_id
                                  AND c.del_yn = 'N'
                                  AND c.couple_type = 'CONNECTED'
                                  AND c.user_id_2 IS NOT NULL
                                WHERE cdc.del_yn = 'N'
                                  AND cdc.selected_yn = 'Y'
                                  AND cdc.issued_date BETWEEN wp.period_start_date AND wp.period_end_date
                                  AND EXISTS (
                                          SELECT 1
                                            FROM daily_card_user_answer a1
                                           WHERE a1.couple_card_id = cdc.couple_card_id
                                             AND a1.user_id = c.user_id_1
                                             AND a1.del_yn = 'N'
                                  )
                                  AND EXISTS (
                                          SELECT 1
                                            FROM daily_card_user_answer a2
                                           WHERE a2.couple_card_id = cdc.couple_card_id
                                             AND a2.user_id = c.user_id_2
                                             AND a2.del_yn = 'N'
                                  )
                           ) AS both_answered_card_count,
                           (
                               SELECT COUNT(*)
                                 FROM couple_daily_card_feedback cdcf
                                WHERE cdcf.del_yn = 'N'
                                  AND cdcf.status = 'COMPLETED'
                                  AND cdcf.completed_at >= wp.period_start_date
                                  AND cdcf.completed_at < DATE_ADD(wp.period_end_date, INTERVAL 1 DAY)
                           ) AS ai_feedback_count
                      FROM weekly_periods wp
                   ) stats
             ORDER BY stats.period_start_date
            """)
    List<WeeklyStatisticsProjection> findWeeklyStatistics(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
