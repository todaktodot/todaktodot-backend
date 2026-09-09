package com.todaktodot.TDTD.admin.vote.repository;

import com.todaktodot.TDTD.domain.login.respository.entity.User;
import com.todaktodot.TDTD.domain.vote.repository.entity.SuspensionStatus;
import com.todaktodot.TDTD.domain.vote.repository.entity.UserSuspensionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminSuspensionRepository extends JpaRepository<UserSuspensionEntity, Long> {

    boolean existsByUserIdAndStatusAndDelYn(Long userId, SuspensionStatus status, String delYn);

    @Modifying
    @Query("""
        UPDATE UserSuspensionEntity us
           SET us.delYn = 'Y',
               us.updDt = CURRENT_TIMESTAMP,
               us.updrId = :adminId
         WHERE us.userId = :userId
           AND us.status = :status
           AND us.delYn = 'N'
    """)
    int softDeleteHistory(@Param("userId") Long userId,
                                  @Param("status") SuspensionStatus status,
                                  @Param("adminId") Long adminId);

    int countByUserIdAndStatus(Long userId, SuspensionStatus status);
}
