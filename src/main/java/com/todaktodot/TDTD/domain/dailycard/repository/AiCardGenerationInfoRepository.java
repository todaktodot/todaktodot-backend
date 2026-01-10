package com.todaktodot.TDTD.domain.dailycard.repository;

import com.todaktodot.TDTD.domain.dailycard.repository.entity.AiCardGenerationInfoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiCardGenerationInfoRepository extends JpaRepository<AiCardGenerationInfoEntity, Long> {

    /**
     * 카드 ID로 생성 정보 조회
     */
    Optional<AiCardGenerationInfoEntity> findByCardId(Long cardId);

    /**
     * 전체 생성 정보 조회 (최신순)
     */
    Page<AiCardGenerationInfoEntity> findAllByOrderByRegDtDesc(Pageable pageable);

    /**
     * AI 모델별 생성 정보 조회
     */
    Page<AiCardGenerationInfoEntity> findByAiModelOrderByRegDtDesc(String aiModel, Pageable pageable);

    /**
     * 프롬프트 ID별 생성 정보 조회
     */
    Page<AiCardGenerationInfoEntity> findByPromptIdOrderByRegDtDesc(Long promptId, Pageable pageable);

    /**
     * AI 모델별 사용 횟수 통계
     */
    @Query("SELECT i.aiModel, COUNT(i) FROM AiCardGenerationInfoEntity i GROUP BY i.aiModel ORDER BY COUNT(i) DESC")
    List<Object[]> countByAiModel();

    /**
     * 프롬프트별 사용 횟수 통계
     */
    @Query("SELECT i.promptId, COUNT(i) FROM AiCardGenerationInfoEntity i GROUP BY i.promptId ORDER BY COUNT(i) DESC")
    List<Object[]> countByPromptId();
}
