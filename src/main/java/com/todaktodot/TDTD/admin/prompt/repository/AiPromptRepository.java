package com.todaktodot.TDTD.admin.prompt.repository;

import com.todaktodot.TDTD.admin.prompt.repository.entity.AiPromptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiPromptRepository extends JpaRepository<AiPromptEntity, Long> {

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
