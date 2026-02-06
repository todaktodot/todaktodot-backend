package com.todaktodot.TDTD.domain.couple.repository;

import com.todaktodot.TDTD.domain.couple.repository.entity.CoupleEntity;
import com.todaktodot.TDTD.domain.couple.repository.entity.CoupleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CoupleRepository extends JpaRepository<CoupleEntity, Long> {

    /**
     * 사용자 ID로 커플 관계 조회 (USER_ID_1 또는 USER_ID_2 중 하나라도 일치)
     * DEL_YN = 'N'인 경우만 조회
     */
    @Query("SELECT c FROM CoupleEntity c WHERE (c.userId1 = :userId OR c.userId2 = :userId) AND c.delYn = 'N'")
    Optional<CoupleEntity> findByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자가 이미 커플 관계인지 확인
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM CoupleEntity c WHERE (c.userId1 = :userId OR c.userId2 = :userId) AND c.delYn = 'N'")
    boolean existsByUserId(@Param("userId") Long userId);

    List<CoupleEntity> findByDelYn(String delYn);

    Page<CoupleEntity> findByDelYn(String delYn, Pageable pageable);

    long countByDelYn(String delYn);

    /**
     * [TDTDBE-55] 사용자의 SOLO 커플 조회 (userId1 기준)
     * 혼자 둘러보기 상태인 커플만 조회
     */
    @Query("SELECT c FROM CoupleEntity c WHERE c.userId1 = :userId AND c.coupleType = 'SOLO' AND c.delYn = 'N'")
    Optional<CoupleEntity> findSoloCoupleByUserId(@Param("userId") Long userId);

    /**
     * [TDTDBE-55] 연결된 커플만 조회 (배치용)
     * CONNECTED 타입이고 삭제되지 않은 커플만 조회
     */
    @Query("SELECT c FROM CoupleEntity c WHERE c.coupleType = 'CONNECTED' AND c.delYn = 'N'")
    List<CoupleEntity> findConnectedCouples();

    /**
     * [TDTDBE-55] 커플 타입으로 조회
     */
    List<CoupleEntity> findByCoupleTypeAndDelYn(CoupleType coupleType, String delYn);
}
