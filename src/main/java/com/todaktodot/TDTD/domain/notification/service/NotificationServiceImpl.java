package com.todaktodot.TDTD.domain.notification.service;

import com.todaktodot.TDTD.domain.notification.dto.PushMessage;
import com.todaktodot.TDTD.domain.notification.dto.reqeust.NotificationSaveRequest;
import com.todaktodot.TDTD.domain.notification.repository.NotificationRepository;
import com.todaktodot.TDTD.domain.notification.repository.entity.NotificationEntity;
import com.todaktodot.TDTD.domain.notification.repository.entity.PushType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService{

    private final NotificationRepository notificationRepository;

    //푸시알림 히스토리 저장
    @Override
    @Transactional
    public void saveNotification(List<NotificationSaveRequest> notificationSaveRequestList) {
        if (notificationSaveRequestList == null || notificationSaveRequestList.isEmpty()) return;

        List<NotificationEntity> notificationEntityList = new ArrayList<>();
        for(NotificationSaveRequest request : notificationSaveRequestList) {
            PushMessage pushMessage = request.getPushMessage();
            if (pushMessage == null) continue;

            String coupleDailyCardId = pushMessage.getData().get("coupleDailyCardId");
            notificationEntityList.add(NotificationEntity.builder()
                    .fcmToken(request.getFcmToken())
                    .coupleDailyCardId(coupleDailyCardId == null ? 0L : Long.parseLong(coupleDailyCardId))
                    .receiveUser(request.getReceiveUser())
                    .title(pushMessage.getTitle())
                    .conent(pushMessage.getBody())
                    .pushType(pushMessage.getPushType())
                    .successYn(request.getSuccessYn())
                    .build());
        }

        notificationRepository.saveAll(notificationEntityList);
    }
}
