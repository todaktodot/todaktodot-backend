package com.todaktodot.TDTD.admin.prompt.repository;

import com.todaktodot.TDTD.admin.prompt.repository.entity.SituationCategoryEntity;
import com.todaktodot.TDTD.domain.dailycard.repository.entity.CardSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SituationCategoryRepository extends JpaRepository<SituationCategoryEntity, Long> {

    @Query("SELECT s FROM SituationCategoryEntity s WHERE s.delYn = 'N' ORDER BY s.subject, s.sortOrder")
    List<SituationCategoryEntity> findAllActive();

    @Query("SELECT s FROM SituationCategoryEntity s WHERE s.subject = :subject AND s.useYn = 'Y' AND s.delYn = 'N' ORDER BY s.sortOrder")
    List<SituationCategoryEntity> findActiveBySubject(@Param("subject") CardSubject subject);

    @Query("SELECT s FROM SituationCategoryEntity s WHERE s.subject = :subject AND s.delYn = 'N' ORDER BY s.sortOrder")
    List<SituationCategoryEntity> findBySubject(@Param("subject") CardSubject subject);

    long countByDelYn(String delYn);

    long countBySubjectAndDelYn(CardSubject subject, String delYn);
}
