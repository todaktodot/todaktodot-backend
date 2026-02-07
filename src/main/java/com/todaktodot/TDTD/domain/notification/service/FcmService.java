package com.todaktodot.TDTD.domain.notification.service;

import com.todaktodot.TDTD.domain.notification.dto.PushMessage;

import java.util.List;

public interface FcmService {

    /**
     * 특정 사용자에게 푸시 알림 전송 (단순 메시지)
     */
    void sendToUser(Long userId, String title, String body);

    /**
     * 특정 사용자에게 푸시 알림 전송 (딥링크 포함)
     */
    void sendToUser(Long userId, PushMessage message);

    /**
     * 특정 사용자들에게 푸시 알림 전송 (단순 메시지)
     */
    void sendToUsers(List<Long> userIds, String title, String body);

    /**
     * 특정 사용자들에게 푸시 알림 전송 (딥링크 포함)
     */
    void sendToUsers(List<Long> userIds, PushMessage message);

    /**
     * 커플에게 푸시 알림 전송 (단순 메시지)
     */
    void sendToCouple(Long coupleId, String title, String body);

    /**
     * 커플에게 푸시 알림 전송 (딥링크 포함)
     */
    void sendToCouple(Long coupleId, PushMessage message);

    /**
     * 특정 토큰으로 직접 푸시 알림 전송
     */
    void sendToToken(String fcmToken, String title, String body);
}
