package com.todaktodot.TDTD.admin.vote.repository;

import com.todaktodot.TDTD.domain.vote.repository.entity.VoteEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminReportRepository extends JpaRepository<VoteEntity, Long> {
    @Query(
            value = """
        SELECT
            V.user_id AS userId,

            /* 신고 누적 수 */
            COUNT(VR.report_id) AS reportedCnt,

            (
            SELECT COUNT(*)
                    FROM vote DV
                    WHERE DV.user_id = V.user_id
                      AND DV.del_yn = 'Y'
            ) AS deletedCnt,

            /* 자동 숨김 처리된 투표 수 */
            COUNT(
                DISTINCT CASE
                    WHEN V.status = 'HIDDEN'
                     AND V.hide_reason = 'AUTO'
                     AND V.del_yn = 'N'
                    THEN V.vote_id
                END
            ) AS autoHiddenCnt,

            /* 최근 신고 일시 */
            MAX(VR.reg_dt) AS latestReportDt,

            /* 현재 정지 상태 */
            CASE
                WHEN EXISTS (
                    SELECT 1
                    FROM user_suspension US
                    WHERE US.user_id = V.user_id
                      AND US.status = 'SUSPENDED'
                      AND US.del_yn = 'N'
                )
                THEN 'SUSPENDED'
                ELSE 'NORMAL'
            END AS status

        FROM vote_report VR

        JOIN vote V
          ON V.vote_id = VR.vote_id
        WHERE VR.del_yn = 'N'
          AND (
                :keyword IS NULL
                OR :keyword = ''
                OR CAST(V.user_id AS CHAR) LIKE CONCAT('%', :keyword, '%')
          )
        GROUP BY V.user_id
        HAVING (
            :status IS NULL
            OR :status = ''
            OR (
                :status = 'SUSPENDED'
                AND EXISTS (
                    SELECT 1
                    FROM user_suspension US
                    WHERE US.user_id = V.user_id
                      AND US.status = 'SUSPENDED'
                      AND US.del_yn = 'N'
                )
            )
            OR (
                :status = 'NORMAL'
                AND NOT EXISTS (
                    SELECT 1
                    FROM user_suspension US
                    WHERE US.user_id = V.user_id
                      AND US.status = 'SUSPENDED'
                      AND US.del_yn = 'N'
                )
            )
        )
        ORDER BY
            CASE
                WHEN :sortBy = 'REPORTED_COUNT'
                THEN COUNT(VR.report_id)
            END DESC,
            CASE
                WHEN :sortBy = 'LATEST_REPORT'
                THEN MAX(VR.reg_dt)
            END DESC,
            V.user_id DESC
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM (
            SELECT V.user_id
            FROM vote_report VR
            JOIN vote V
              ON V.vote_id = VR.vote_id
            WHERE VR.del_yn = 'N'
              AND (
                    :keyword IS NULL
                    OR :keyword = ''
                    OR CAST(V.user_id AS CHAR) LIKE CONCAT('%', :keyword, '%')
              )
            GROUP BY V.user_id
            HAVING (
                :status IS NULL
                OR :status = ''
                OR (
                    :status = 'SUSPENDED'
                    AND EXISTS (
                        SELECT 1
                        FROM user_suspension US
                        WHERE US.user_id = V.user_id
                          AND US.status = 'SUSPENDED'
                          AND US.del_yn = 'N'
                    )
                )
                OR (
                    :status = 'NORMAL'
                    AND NOT EXISTS (
                        SELECT 1
                        FROM user_suspension US
                        WHERE US.user_id = V.user_id
                          AND US.status = 'SUSPENDED'
                          AND US.del_yn = 'N'
                    )
                )
            )
        ) T
        """, nativeQuery = true
    )
    Page<AdminReportProjection> findAllReportedUsers(@Param("status") String status,
                                                     @Param("sortBy") String sortBy,
                                                     @Param("keyword") String keyword,
                                                     Pageable pageable);

    //삭제된 투표 포함하여 집계 -> 확인필요
    @Query(value = """
        SELECT COUNT(DISTINCT V.user_id)
        FROM vote_report VR
        JOIN vote V
          ON V.vote_id = VR.vote_id
        WHERE VR.del_yn = 'N'
    """, nativeQuery = true)
    Integer countReportedUsers();

    @Query(value = """
        SELECT COUNT(DISTINCT V.user_id)
        FROM vote_report VR
        JOIN vote V
          ON V.vote_id = VR.vote_id
        WHERE VR.del_yn = 'N'
          AND V.del_yn = 'N'
          AND NOT EXISTS (
              SELECT 1
              FROM user_suspension US
              WHERE US.user_id = V.user_id
                AND US.status = 'SUSPENDED'
                AND US.del_yn = 'N'
          )
    """, nativeQuery = true)
    Integer countNormalUsers();

    @Query(value = """
        SELECT COUNT(DISTINCT V.user_id)
        FROM vote_report VR
        JOIN vote V
          ON V.vote_id = VR.vote_id
        WHERE VR.del_yn = 'N'
          AND V.del_yn = 'N'
          AND EXISTS (
              SELECT 1
              FROM user_suspension US
              WHERE US.user_id = V.user_id
                AND US.status = 'SUSPENDED'
                AND US.del_yn = 'N'
          )
    """, nativeQuery = true)
    Integer countSuspendedUsers();

    @Query(value = """
        SELECT COUNT(DISTINCT US.user_id)
        FROM user_suspension US
        WHERE US.status = 'SUSPENDED'
          AND US.del_yn = 'N'
          AND US.suspended_dt >= DATE_SUB(
                CURDATE(),
                INTERVAL WEEKDAY(CURDATE()) DAY
          )
          AND US.suspended_dt < DATE_ADD(
                DATE_SUB(
                    CURDATE(),
                    INTERVAL WEEKDAY(CURDATE()) DAY
                ),
                INTERVAL 7 DAY
          )
          AND EXISTS (
              SELECT 1
              FROM vote V
              JOIN vote_report VR
                ON VR.vote_id = V.vote_id
               AND VR.del_yn = 'N'
              WHERE V.user_id = US.user_id
          )
    """, nativeQuery = true)
    Integer countWeeklySuspendedUsers();

    @Query(value = """
    SELECT
        U.id AS userId,
        (
            SELECT COUNT(*)
            FROM vote V
            WHERE V.user_id = U.id
              AND V.del_yn = 'N'
        ) AS voteCnt,
        (
            SELECT COUNT(VR.report_id)
            FROM vote V
            JOIN vote_report VR
              ON VR.vote_id = V.vote_id
            WHERE V.user_id = U.id
        ) AS reportedCnt,
        (
            SELECT COUNT(*)
            FROM vote V
            WHERE V.user_id = U.id
              AND V.del_yn = 'Y'
        ) AS deletedCnt,
        (
            SELECT COUNT(*)
            FROM vote V
            WHERE V.user_id = U.id
              AND V.status = 'HIDDEN'
              AND V.hide_reason = 'AUTO'
              AND V.del_yn = 'N'
        ) AS autoHiddenCnt,
        (
            SELECT MAX(VR.reg_dt)
            FROM vote V
            JOIN vote_report VR
              ON VR.vote_id = V.vote_id
             AND VR.del_yn = 'N'
            WHERE V.user_id = U.id
        ) AS latestReportDt,
        (
            SELECT US.suspended_dt
            FROM user_suspension US
            WHERE US.user_id = U.id
              AND US.status = 'SUSPENDED'
              AND US.del_yn = 'N'
            ORDER BY US.suspended_dt DESC
            LIMIT 1
        ) AS suspendedDt,
        U.reg_dt AS joinedDt
    FROM users U
    WHERE U.id = :userId
    """, nativeQuery = true)
    AdminReportDetailProjection findReportDetail(@Param("userId") Long userId);

    @Query(value = """
    SELECT
        V.vote_id AS voteId,
        V.title AS title,
        /* 해당 투표의 신고 수 */
        COUNT(VR.report_id) AS reportedCnt,
        CASE
            WHEN V.del_yn = 'Y'
                THEN 'DELETED'
            WHEN V.status = 'HIDDEN'
             AND V.hide_reason = 'AUTO'
                THEN 'AUTO_HIDDEN'
            WHEN V.status = 'HIDDEN'
             AND V.hide_reason = 'ADMIN'
                THEN 'ADMIN_HIDDEN'
            ELSE 'ACTIVE'
        END AS status,
        V.reg_dt AS regDt
    FROM vote V
    JOIN vote_report VR
      ON VR.vote_id = V.vote_id
     AND VR.del_yn = 'N'
    WHERE V.user_id = :userId
    GROUP BY
        V.vote_id,
        V.title,
        V.del_yn,
        V.status,
        V.hide_reason,
        V.reg_dt
    ORDER BY
        V.reg_dt DESC,
        V.vote_id DESC
    """, nativeQuery = true)
    List<AdminReportVoteProjection> findReportedVotes(@Param("userId") Long userId);

    @Query(value = """
        SELECT
            US.reason AS reason,
            US.reg_dt AS regDt
        FROM user_suspension US
        WHERE US.user_id = :userId
          AND US.status = 'SUSPENDED'
          AND US.del_yn = 'N'
        LIMIT 1
    """, nativeQuery = true)
    AdminSuspensionProjection findActiveSuspensionByUserId(@Param("userId") Long userId);

    @Query(value = """
        SELECT
            US.reason AS reason,
            US.reg_dt AS regDt
        FROM user_suspension US
        WHERE US.user_id = :userId
          AND US.status = 'RELEASE'
          AND US.del_yn = 'N'
        LIMIT 1
    """, nativeQuery = true)
    AdminSuspensionProjection findActiveReleaseByUserId(@Param("userId") Long userId);
}
