package com.todaktodot.TDTD.domain.vote.repository;

import com.todaktodot.TDTD.domain.vote.repository.entity.VoteCategory;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteEntity;
import com.todaktodot.TDTD.domain.vote.repository.entity.VoteStatus;
import com.todaktodot.TDTD.domain.vote.repository.projection.VoteCursorProjection;
import com.todaktodot.TDTD.domain.vote.repository.projection.VoteProjection;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<VoteEntity, Long> {

    Optional<VoteEntity> findByVoteIdAndDelYn(Long voteId, String delYn);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM VoteEntity v WHERE v.voteId = :voteId AND v.delYn = 'N'")
    Optional<VoteEntity> findByVoteIdForUpdate(@Param("voteId") Long voteId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE VoteEntity v
            SET v.participantCnt = v.participantCnt + 1, v.updrId = :userId
            WHERE v.voteId = :voteId
            """)
    void increaseParticipantCnt(@Param("voteId") Long voteId, @Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE VoteEntity v
            SET v.participantCnt = v.participantCnt - 1, v.updrId = :userId
            WHERE v.voteId = :voteId AND v.participantCnt > 0
            """)
    void decreaseParticipantCnt(@Param("voteId") Long voteId, @Param("userId") Long userId);

    long countByUserIdAndRegDtBetweenAndDelYn(Long userId, LocalDateTime startDateTime, LocalDateTime endDateTime, String delYn);

    @Query(value = """
        SELECT
            V.VOTE_ID AS voteId,
            V.REG_DT AS createdAt,
            V.PARTICIPANT_CNT AS participantCnt
        FROM VOTE V
        WHERE V.DEL_YN = 'N'
          AND V.STATUS = 'POSTED'
          /* 카테고리 */
          AND  V.CATEGORY IN (:categories)
          /* 내가 작성한 투표 여부 */
          AND (
                :isMine IS NULL
                OR (:isMine = TRUE AND V.USER_ID = :userId)
          )
          /* 투표 진행 상태 */
          AND (
                :voteStatus IS NULL
                OR (:voteStatus = 'OPEN' AND V.CLOSED_AT > NOW())
                OR (:voteStatus = 'CLOSED' AND V.CLOSED_AT <= NOW())
          )
          AND NOT EXISTS (
                      SELECT 1
                      FROM VOTE_REPORT VR
                      WHERE VR.VOTE_ID = V.VOTE_ID
                        AND VR.USER_ID = :userId
                        AND VR.DEL_YN = 'N'
         )
        ORDER BY
            V.REG_DT DESC,
            V.VOTE_ID DESC
        LIMIT :size
    """, nativeQuery = true)
    List<VoteCursorProjection> findFirstByLatest(
            @Param("userId") Long userId,
            @Param("category") List<VoteCategory> categories,
            @Param("isMine") Boolean isMine,
            @Param("voteStatus") VoteStatus voteStatus,
            @Param("size") int size
    );

    @Query(value = """
        SELECT
            V.VOTE_ID AS voteId,
            V.REG_DT AS createdAt,
            V.PARTICIPANT_CNT AS participantCnt
        FROM VOTE V
        WHERE V.DEL_YN = 'N'
          AND V.STATUS = 'POSTED'
          AND  V.CATEGORY IN (:categories)
          AND (
                :isMine IS NULL
                OR (:isMine = TRUE AND V.USER_ID = :userId)
          )
          AND (
                :voteStatus IS NULL
                OR (:voteStatus = 'OPEN' AND V.CLOSED_AT > NOW())
                OR (:voteStatus = 'CLOSED' AND V.CLOSED_AT <= NOW())
          )
          AND NOT EXISTS (
              SELECT 1
              FROM VOTE_REPORT VR
              WHERE VR.VOTE_ID = V.VOTE_ID
                AND VR.USER_ID = :userId
                AND VR.DEL_YN = 'N'
         )
          /* 커서 */
          AND (
                V.REG_DT < :cursorCreatedAt
                OR (
                    V.REG_DT = :cursorCreatedAt
                    AND V.VOTE_ID < :cursorVoteId
                )
          )
        ORDER BY
            V.REG_DT DESC,
            V.VOTE_ID DESC
        LIMIT :size
    """, nativeQuery = true)
    List<VoteCursorProjection> findNextByLatest(
            @Param("userId") Long userId,
            @Param("category") List<VoteCategory> categories,
            @Param("isMine") Boolean isMine,
            @Param("voteStatus") VoteStatus voteStatus,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorVoteId") Long cursorVoteId,
            @Param("size") int size
    );

    @Query(value = """
        SELECT
            V.VOTE_ID AS voteId,
            V.REG_DT AS createdAt,
            V.PARTICIPANT_CNT AS participantCnt
        FROM VOTE V
        WHERE V.DEL_YN = 'N'
          AND V.STATUS = 'POSTED'
          AND V.CATEGORY IN (:categories)
          AND (
                :isMine IS NULL
                OR (:isMine = TRUE AND V.USER_ID = :userId)
          )
          AND (
                :voteStatus IS NULL
                OR (:voteStatus = 'OPEN' AND V.CLOSED_AT > NOW())
                OR (:voteStatus = 'CLOSED' AND V.CLOSED_AT <= NOW())
          )
          AND NOT EXISTS (
              SELECT 1
              FROM VOTE_REPORT VR
              WHERE VR.VOTE_ID = V.VOTE_ID
                AND VR.USER_ID = :userId
                AND VR.DEL_YN = 'N'
         )
        ORDER BY
            V.PARTICIPANT_CNT DESC,
            V.VOTE_ID DESC
        LIMIT :size
    """, nativeQuery = true)
    List<VoteCursorProjection> findFirstByPopular(
            @Param("userId") Long userId,
            @Param("category") List<VoteCategory> categories,
            @Param("isMine") Boolean isMine,
            @Param("voteStatus") VoteStatus voteStatus,
            @Param("size") int size
    );

    @Query(value = """
        SELECT
            V.VOTE_ID AS voteId,
            V.REG_DT AS createdAt,
            V.PARTICIPANT_CNT AS participantCnt
        FROM VOTE V
        WHERE V.DEL_YN = 'N'
          AND V.STATUS = 'POSTED'
          AND V.CATEGORY IN (:categories)
          AND (
                :isMine IS NULL
                OR (:isMine = TRUE AND V.USER_ID = :userId)
          )
          AND (
                :voteStatus IS NULL
                OR (:voteStatus = 'OPEN' AND V.CLOSED_AT > NOW())
                OR (:voteStatus = 'CLOSED' AND V.CLOSED_AT <= NOW())
          )
          AND NOT EXISTS (
              SELECT 1
              FROM VOTE_REPORT VR
              WHERE VR.VOTE_ID = V.VOTE_ID
                AND VR.USER_ID = :userId
                AND VR.DEL_YN = 'N'
         )
          AND (
                V.PARTICIPANT_CNT < :cursorParticipantCnt
                OR (
                    V.PARTICIPANT_CNT = :cursorParticipantCnt
                    AND V.VOTE_ID < :cursorVoteId
                )
          )
        ORDER BY
            V.PARTICIPANT_CNT DESC,
            V.VOTE_ID DESC
        LIMIT :size
    """, nativeQuery = true)
    List<VoteCursorProjection> findNextByPopular(
            @Param("userId") Long userId,
            @Param("category") List<VoteCategory> categories,
            @Param("isMine") Boolean isMine,
            @Param("voteStatus") VoteStatus voteStatus,
            @Param("cursorParticipantCnt") Integer cursorParticipantCnt,
            @Param("cursorVoteId") Long cursorVoteId,
            @Param("size") int size
    );

    @Query(value = """
        SELECT
            V.VOTE_ID AS voteId,
            V.RANDOM_NICKNAME AS nickname,
            V.CATEGORY AS category,
            CASE
                WHEN V.CLOSED_AT > NOW() THEN 'ACTIVE'
                ELSE 'CLOSED'
            END AS status,
            V.TITLE AS title,
            COALESCE(L.LIKE_CNT, 0) AS likeCnt,
            V.PARTICIPANT_CNT AS participantCnt,
            COALESCE(R.REPORT_CNT, 0) AS reportCnt,
            V.CLOSED_AT AS closedAt,
            V.REG_DT AS createdAt,
            CASE
                WHEN V.USER_ID = :userId THEN 'Y'
                ELSE 'N'
            END AS isMine,
            CASE
                WHEN US.SELECT_ID IS NOT NULL THEN 'Y'
                ELSE 'N'
            END AS hasVoted,
            CASE
                WHEN UL.LIKE_ID IS NOT NULL THEN 'Y'
                ELSE 'N'
            END AS hasLiked,
            O.OPTION_ID AS optionId,
            O.CONTENT AS content,
            O.SORT_ORDER AS sortOrder,
            COALESCE(OS.VOTE_CNT, 0) AS voteCnt,
            CASE
                WHEN V.PARTICIPANT_CNT > 0
                THEN ROUND(COALESCE(OS.VOTE_CNT, 0) * 100.0 / V.PARTICIPANT_CNT,2)
                ELSE 0
            END AS voteRate,
            CASE
                WHEN US.OPTION_ID = O.OPTION_ID THEN 'Y'
                ELSE 'N'
            END AS isSelected
        FROM VOTE V
        INNER JOIN VOTE_OPTION O
            ON O.VOTE_ID = V.VOTE_ID
           AND O.DEL_YN = 'N'
        LEFT JOIN (
            SELECT
                VOTE_ID,
                COUNT(*) AS LIKE_CNT
            FROM VOTE_LIKE
            WHERE DEL_YN = 'N'
            GROUP BY VOTE_ID
        ) L
            ON L.VOTE_ID = V.VOTE_ID
        LEFT JOIN (
            SELECT
                VOTE_ID,
                COUNT(*) AS REPORT_CNT
            FROM VOTE_REPORT
            WHERE DEL_YN = 'N'
            GROUP BY VOTE_ID
        ) R
            ON R.VOTE_ID = V.VOTE_ID
        LEFT JOIN (
            SELECT
                OPTION_ID,
                COUNT(*) AS VOTE_CNT
            FROM VOTE_SELECT
            WHERE DEL_YN = 'N'
            GROUP BY OPTION_ID
        ) OS
            ON OS.OPTION_ID = O.OPTION_ID
        LEFT JOIN VOTE_SELECT US
            ON US.VOTE_ID = V.VOTE_ID
           AND US.USER_ID = :userId
           AND US.DEL_YN = 'N'
        LEFT JOIN VOTE_LIKE UL
            ON UL.VOTE_ID = V.VOTE_ID
           AND UL.USER_ID = :userId
           AND UL.DEL_YN = 'N'
        WHERE V.VOTE_ID IN (:voteIds)
        ORDER BY
            V.VOTE_ID DESC,
            O.SORT_ORDER ASC
    """, nativeQuery = true)
    List<VoteProjection> selectVoteDetails(
            @Param("voteIds") List<Long> voteIds,
            @Param("userId") Long userId
    );
}
