package com.todaktodot.TDTD.domain.notification.repository;

import com.todaktodot.TDTD.domain.notification.repository.entity.NotificationEntity;
import com.todaktodot.TDTD.domain.notification.repository.entity.PushType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findAllByCoupleDailyCardIdAndPushTypeAndSuccessYn(Long coupleDailyCardId, PushType pushType, String successYn);
}
