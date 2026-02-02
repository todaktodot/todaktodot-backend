package com.todaktodot.TDTD.admin.prompt.repository;

import com.todaktodot.TDTD.admin.prompt.repository.entity.AiPromptEntity;
import com.todaktodot.TDTD.admin.prompt.repository.entity.PromptType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiPromptRepository extends JpaRepository<AiPromptEntity, Long> {

    // ==================== 그룹 기반 쿼리 (신규) ====================

    @Query("SELECT p FROM AiPromptEntity p WHERE p.delYn = 'N' " +
           "AND p.promptId IN (SELECT MAX(p2.promptId) FROM AiPromptEntity p2 " +
           "WHERE p2.delYn = 'N' GROUP BY p2.promptGroupId) " +
           "ORDER BY p.promptGroupId DESC")
    List<AiPromptEntity> findLatestPerGroup();

    @Query("SELECT p FROM AiPromptEntity p WHERE p.promptType = :promptType " +
           "AND p.useYn = 'Y' AND p.delYn = 'N' " +
           "AND p.promptId IN (SELECT MAX(p2.promptId) FROM AiPromptEntity p2 " +
           "WHERE p2.promptType = :promptType AND p2.useYn = 'Y' AND p2.delYn = 'N' " +
           "GROUP BY p2.promptGroupId) " +
           "ORDER BY p.promptGroupId DESC")
    List<AiPromptEntity> findLatestActivePerGroupByType(@Param("promptType") PromptType promptType);

    @Query("SELECT p FROM AiPromptEntity p WHERE p.promptGroupId = :groupId " +
           "AND p.delYn = 'N' ORDER BY p.version DESC")
    List<AiPromptEntity> findAllByPromptGroupId(@Param("groupId") Long promptGroupId);

    @Query("SELECT MAX(p.version) FROM AiPromptEntity p " +
           "WHERE p.promptGroupId = :groupId AND p.delYn = 'N'")
    Integer findMaxVersionByPromptGroupId(@Param("groupId") Long promptGroupId);

    @Query("SELECT COALESCE(MAX(p.promptGroupId), 0) + 1 FROM AiPromptEntity p")
    Long findNextPromptGroupId();

    @Query("SELECT COUNT(DISTINCT p.promptGroupId) FROM AiPromptEntity p WHERE p.delYn = 'N'")
    long countDistinctGroups();

    // ==================== 기존 메서드 (하위 호환용) ====================

    @Query("SELECT p FROM AiPromptEntity p WHERE p.delYn = 'N' ORDER BY p.promptName, p.version DESC")
    List<AiPromptEntity> findAllActive();

    @Query("SELECT p FROM AiPromptEntity p WHERE p.promptName = :promptName AND p.useYn = 'Y' AND p.delYn = 'N' ORDER BY p.version DESC")
    List<AiPromptEntity> findActiveByPromptName(@Param("promptName") String promptName);

    Optional<AiPromptEntity> findTopByPromptNameAndUseYnAndDelYnOrderByVersionDesc(
            String promptName, String useYn, String delYn);

    @Query("SELECT MAX(p.version) FROM AiPromptEntity p WHERE p.promptName = :promptName AND p.delYn = 'N'")
    Integer findMaxVersionByPromptName(@Param("promptName") String promptName);

    long countByDelYn(String delYn);
}
