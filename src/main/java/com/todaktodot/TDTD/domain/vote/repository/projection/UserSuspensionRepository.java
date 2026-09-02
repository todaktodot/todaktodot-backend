package com.todaktodot.TDTD.domain.vote.repository.projection;

import com.todaktodot.TDTD.domain.vote.repository.entity.SuspensionStatus;
import com.todaktodot.TDTD.domain.vote.repository.entity.UserSuspensionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSuspensionRepository extends JpaRepository<UserSuspensionEntity, Long> {
    boolean existsByUserIdAndStatusAndDelYn(Long userId, SuspensionStatus status, String delYn);
}
