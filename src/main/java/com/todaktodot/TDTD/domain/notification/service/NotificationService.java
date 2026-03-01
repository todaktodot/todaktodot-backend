package com.todaktodot.TDTD.domain.notification.service;

import com.todaktodot.TDTD.domain.notification.dto.reqeust.NotificationSaveRequest;

import java.util.List;

public interface NotificationService {
    /**
     * 푸시알림 전송 후 알림객체 저장
     */
    void saveNotification(List<NotificationSaveRequest> notificationSaveRequestList);
}
