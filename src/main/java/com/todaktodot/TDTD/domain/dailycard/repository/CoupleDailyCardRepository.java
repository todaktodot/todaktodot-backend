package com.todaktodot.TDTD.domain.dailycard.repository;

import com.todaktodot.TDTD.domain.dailycard.repository.entity.CoupleDailyCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CoupleDailyCardRepository extends JpaRepository<CoupleDailyCardEntity, Long> {

    Optional<CoupleDailyCardEntity> findByCoupleIdAndIssuedDateAndDelYn(
            Long coupleId, LocalDate issuedDate, String delYn);

    List<CoupleDailyCardEntity> findAllByCoupleIdAndIssuedDateAndDelYn(
            Long coupleId, LocalDate issuedDate, String delYn);

    boolean existsByCoupleIdAndIssuedDateAndDelYn(Long coupleId, LocalDate issuedDate, String delYn);

    long countByCoupleIdAndIssuedDateAndDelYn(Long coupleId, LocalDate issuedDate, String delYn);

    @EntityGraph(attributePaths = "dailyCard")
    Optional<CoupleDailyCardEntity> findTopByCoupleIdAndDelYnOrderByIssuedDateDesc(Long coupleId, String delYn);

    @EntityGraph(attributePaths = "dailyCard")
    List<CoupleDailyCardEntity> findAllByCoupleIdAndIssuedDateAndDelYnOrderByCoupleCardIdAsc(
            Long coupleId, LocalDate issuedDate, String delYn);

    List<CoupleDailyCardEntity> findAllByCoupleIdAndDelYnOrderByIssuedDateDescCoupleCardIdDesc(
            Long coupleId, String delYn);

    @Query("SELECT c FROM CoupleDailyCardEntity c " +
           "LEFT JOIN FETCH c.dailyCard " +
           "WHERE c.coupleCardId = :coupleCardId AND c.delYn = 'N'")
    Optional<CoupleDailyCardEntity> findByIdWithDailyCard(@Param("coupleCardId") Long coupleCardId);
}
