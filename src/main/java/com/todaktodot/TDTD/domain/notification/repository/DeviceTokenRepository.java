package com.todaktodot.TDTD.domain.notification.repository;

import com.todaktodot.TDTD.domain.notification.repository.entity.DeviceTokenEntity;
import com.todaktodot.TDTD.domain.notification.repository.entity.DeviceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceTokenEntity, Long> {

    Optional<DeviceTokenEntity> findByUserIdAndDeviceTypeAndDelYn(Long userId, DeviceType deviceType, String delYn);

    Optional<DeviceTokenEntity> findByFcmTokenAndDelYn(String fcmToken, String delYn);

    @Query("SELECT dt FROM DeviceTokenEntity dt WHERE dt.user.id = :userId AND dt.isActive = true AND dt.delYn = 'N'")
    List<DeviceTokenEntity> findActiveTokensByUserId(@Param("userId") Long userId);

    @Query("SELECT dt FROM DeviceTokenEntity dt WHERE dt.user.id IN :userIds AND dt.isActive = true AND dt.delYn = 'N'")
    List<DeviceTokenEntity> findActiveTokensByUserIds(@Param("userIds") List<Long> userIds);
}
