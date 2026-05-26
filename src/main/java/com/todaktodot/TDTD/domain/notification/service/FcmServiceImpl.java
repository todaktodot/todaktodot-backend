package com.todaktodot.TDTD.domain.notification.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import com.todaktodot.TDTD.domain.couple.repository.CoupleRepository;
import com.todaktodot.TDTD.domain.couple.repository.entity.CoupleEntity;
import com.todaktodot.TDTD.domain.login.respository.UserRepository;
import com.todaktodot.TDTD.domain.login.respository.entity.User;
import com.todaktodot.TDTD.domain.notification.dto.PushMessage;
import com.todaktodot.TDTD.domain.notification.dto.reqeust.NotificationSaveRequest;
import com.todaktodot.TDTD.domain.notification.repository.DeviceTokenRepository;
import com.todaktodot.TDTD.domain.notification.repository.entity.DeviceTokenEntity;
import com.todaktodot.TDTD.domain.notification.repository.entity.PushType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmServiceImpl implements FcmService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final NotificationService notificationService;
    private final CoupleRepository coupleRepository;
    private final UserRepository userRepository;

    @Override
    @Async
    public void sendToUser(Long userId, String title, String body) {
        if (!isFirebaseInitialized()) return;
        if (!isNotificationEnabled(userId)) return;

        List<String> fcmTokens = getActiveTokens(userId);
        if (fcmTokens.isEmpty()) return;

        sendMulticast(fcmTokens, title, body, Map.of(), userId, null);
    }

    @Override
    @Async
    public void sendToUser(Long userId, PushMessage message) {
        if (!isFirebaseInitialized()) return;

        boolean notificationEnabled = isNotificationEnabled(userId);
        if (!notificationEnabled) {
            if (!PushType.CONNECT_COUPLE.equals(message.getPushType())) return;
        }
        if (!canSendPush(userId, message)) return;

        List<String> fcmTokens = getActiveTokens(userId);
        if (fcmTokens.isEmpty()) return;

        List<NotificationSaveRequest> saveRequestList = new ArrayList<>();
        for (String fcmToken : fcmTokens) {
            saveRequestList.add(mappingToSaveRequest(fcmToken, userId, message));
        }

        if (!notificationEnabled) {
            // 알림 비동의이지만 '커플 연결 푸시'인 경우 -> Silent 푸시
            sendMulticastSlient(fcmTokens, message.getData(), userId, saveRequestList);
        } else {
            String title = buildTitle(message);
            sendMulticast(fcmTokens, title, message.getBody(), message.getData(), userId, saveRequestList);
        }
    }

    @Override
    @Async
    public void sendToUsers(List<Long> userIds, String title, String body) {
        if (!isFirebaseInitialized()) return;

        List<Long> enabledUserIds = filterNotificationEnabledUsers(userIds);
        if (enabledUserIds.isEmpty()) return;

        Map<Long, List<String>> fcmTokensMap = getActiveTokens(enabledUserIds);
        if (fcmTokensMap.isEmpty()) return;

        List<String> fcmTokens = fcmTokensMap.values().stream()
                .flatMap(List::stream)    // 여러 개의 List<String>을 하나의 Stream<String>으로 평탄화
                .collect(Collectors.toList());

        sendMulticast(fcmTokens, title, body, Map.of(), null, null);
    }

    @Override
    @Async
    public void sendToUsers(List<Long> userIds, PushMessage message) {
        if (!isFirebaseInitialized()) return;

        List<Long> enabledUserIds = filterNotificationEnabledUsers(userIds);
        if (enabledUserIds.isEmpty()) return;

        // 광고성 푸시인 경우 마케팅 동의한 사용자만 필터링
        if (message.getPushType().isAdvertising()) {
            enabledUserIds = filterMarketingConsentedUsers(enabledUserIds);
            if (enabledUserIds.isEmpty()) {
                log.debug("마케팅 동의한 사용자가 없습니다");
                return;
            }
        }

        List<String> allFcmTokens = new ArrayList<>();
        List<NotificationSaveRequest> saveRequestList = new ArrayList<>();
        Map<Long, List<String>> fcmTokensMap = getActiveTokens(enabledUserIds);
        if (fcmTokensMap.isEmpty()) return;

        for (Long enableUserId : enabledUserIds) {
            List<String> fcmTokens = fcmTokensMap.getOrDefault(enableUserId, List.of());

            if (fcmTokens.isEmpty()) continue;

            //광고성이 아닌 경우
            if (!message.getPushType().isAdvertising()) {
                for (String fcmToken : fcmTokens) {
                    saveRequestList.add(mappingToSaveRequest(fcmToken, enableUserId, message));
                }
            }
            allFcmTokens.addAll(fcmTokens);
        }

        String title = buildTitle(message);
        sendMulticast(allFcmTokens, title, message.getBody(), message.getData(), null, saveRequestList);
    }

    @Override
    @Async
    public void sendToCouple(Long coupleId, String title, String body) {
        List<Long> userIds = getCoupleUserIds(coupleId);
        if (userIds.isEmpty()) return;

        sendToUsers(userIds, title, body);
        log.info("커플 푸시 발송 완료 - coupleId: {}", coupleId);
    }

    @Override
    @Async
    public void sendToCouple(Long coupleId, PushMessage message) {
        List<Long> userIds = getCoupleUserIds(coupleId);
        if (userIds.isEmpty()) return;

        sendToUsers(userIds, message);
        log.info("커플 푸시 발송 완료 - coupleId: {}, pushType: {}", coupleId, message.getPushType());
    }

    @Override
    @Async
    public void sendToToken(String fcmToken, String title, String body) {
        if (!isFirebaseInitialized()) return;

        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("푸시 발송 성공 - messageId: {}", response);
        } catch (FirebaseMessagingException e) {
            log.error("푸시 발송 실패 - token: {}, error: {}", fcmToken, e.getMessage());
            handleFailedToken(fcmToken, e);
        }
    }

    // ============ Private Methods ============

    private List<Long> getCoupleUserIds(Long coupleId) {
        CoupleEntity couple = coupleRepository.findById(coupleId).orElse(null);
        if (couple == null) {
            log.warn("커플을 찾을 수 없습니다 - coupleId: {}", coupleId);
            return List.of();
        }

        List<Long> userIds = new ArrayList<>();
        userIds.add(couple.getUserId1());
        if (couple.getUserId2() != null) {
            userIds.add(couple.getUserId2());
        }
        return userIds;
    }

    private List<String> getActiveTokens(Long userId) {
        List<DeviceTokenEntity> tokens = deviceTokenRepository.findActiveTokensByUserId(userId);
        if (tokens.isEmpty()) {
            log.debug("활성화된 토큰이 없습니다 - userId: {}", userId);
            return List.of();
        }
        return tokens.stream().map(DeviceTokenEntity::getFcmToken).toList();
    }

    private Map<Long, List<String>> getActiveTokens(List<Long> userIds) {
        List<DeviceTokenEntity> tokens = deviceTokenRepository.findActiveTokensByUserIdsWithUser(userIds);
        if (tokens.isEmpty()) {
            log.debug("활성화된 토큰이 없습니다 - userIds: {}", userIds);
            return Map.of();
        }
        //return tokens.stream().map(DeviceTokenEntity::getFcmToken).toList();
        return tokens.stream().collect(Collectors.groupingBy(
                d -> d.getUser().getId(),
                Collectors.mapping(DeviceTokenEntity::getFcmToken, Collectors.toList())
        ));
    }

    private boolean isNotificationEnabled(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.debug("사용자를 찾을 수 없습니다 - userId: {}", userId);
            return false;
        }

        // alarmYN이 null이거나 "Y"이면 알림 허용 (기본값은 허용)
        boolean enabled = user.getInfoAlarmYN() == null || "Y".equals(user.getInfoAlarmYN());
        if (!enabled) {
            log.debug("알림이 비활성화된 사용자입니다 - userId: {}", userId);
        }
        return enabled;
    }

    private List<Long> filterNotificationEnabledUsers(List<Long> userIds) {
        List<User> users = userRepository.findByIdIn(userIds);
        return users.stream()
                .filter(user -> user.getInfoAlarmYN() == null || "Y".equals(user.getInfoAlarmYN()))
                .map(User::getId)
                .toList();
    }

    /**
     * 마케팅 동의한 사용자만 필터링
     * TODO: User 엔티티에 marketingYN 필드 추가 후 실제 체크 로직 구현
     */
    private List<Long> filterMarketingConsentedUsers(List<Long> userIds) {
        // 현재는 마케팅 동의 필드가 없으므로 빈 리스트 반환 (광고성 푸시 발송 안 함)
        // User 엔티티에 marketingYN 필드 추가 후 아래 로직 활성화:
        // List<User> users = userRepository.findByIdIn(userIds);
        // return users.stream()
        //         .filter(user -> "Y".equals(user.getMarketingYN()))
        //         .map(User::getId)
        //         .toList();
        log.warn("마케팅 동의 필드(marketingYN)가 아직 구현되지 않아 광고성 푸시를 발송하지 않습니다");
        return List.of();
    }

    /**
     * 푸시 발송 가능 여부 확인 (광고성 푸시인 경우 마케팅 동의 체크)
     */
    private boolean canSendPush(Long userId, PushMessage message) {
        if (message.getPushType().isAdvertising()) {
            // TODO: User 엔티티에 marketingYN 필드 추가 후 실제 체크 로직 구현
            log.debug("광고성 푸시는 마케팅 동의 필드(marketingYN) 구현 후 발송 가능합니다 - userId: {}", userId);
            return false;
        }
        return true;
    }

    /**
     * 푸시 제목 생성 (광고성인 경우 "(광고)" 접두사 추가)
     */
    private String buildTitle(PushMessage message) {
        if (message.getPushType().isAdvertising()) {
            return "(광고) " + message.getTitle();
        }
        return message.getTitle();
    }

    private void sendMulticast(List<String> fcmTokens, String title, String body,
                               Map<String, String> data, Long userId, List<NotificationSaveRequest> saveRequest) {

        MulticastMessage.Builder builder = MulticastMessage.builder()
                .addAllTokens(fcmTokens)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build());

        // Data payload 추가 (딥링크용)
        if (data != null && !data.isEmpty()) {
            builder.putAllData(data);
        }

        try {
            BatchResponse response = FirebaseMessaging.getInstance()
                    .sendEachForMulticast(builder.build());
            log.info("푸시 발송 완료 - 성공: {}, 실패: {}",
                    response.getSuccessCount(), response.getFailureCount());

            if (response.getFailureCount() > 0) {
                handleFailedTokens(fcmTokens, response, saveRequest);
            }
            savePushAlarm(saveRequest);
        } catch (FirebaseMessagingException e) {
            log.error("푸시 멀티캐스트 발송 실패 - userId: {}, error: {}", userId, e.getMessage());
        }
    }

    private void sendMulticastSlient(List<String> fcmTokens,
                               Map<String, String> data, Long userId, List<NotificationSaveRequest> saveRequest) {

        MulticastMessage.Builder builder = MulticastMessage.builder()
                .addAllTokens(fcmTokens);

        // Data payload 추가
        if (data != null && !data.isEmpty()) {
            builder.putAllData(data);
        }

        try {
            BatchResponse response = FirebaseMessaging.getInstance()
                    .sendEachForMulticast(builder.build());
            log.info("푸시 발송 완료 - 성공: {}, 실패: {}",
                    response.getSuccessCount(), response.getFailureCount());

            if (response.getFailureCount() > 0) {
                handleFailedTokens(fcmTokens, response, saveRequest);
            }
            savePushAlarm(saveRequest);
        } catch (FirebaseMessagingException e) {
            log.error("푸시 멀티캐스트 발송 실패 - userId: {}, error: {}", userId, e.getMessage());
        }
    }

    private void handleFailedTokens(List<String> tokens, BatchResponse response, List<NotificationSaveRequest> saveRequestList) {
        List<SendResponse> responses = response.getResponses();
        for (int i = 0; i < responses.size(); i++) {
            if (!responses.get(i).isSuccessful()) {
                String failedToken = tokens.get(i);
                if (saveRequestList != null) {
                    saveRequestList.get(i).setSuccessYn("N");
                }
                FirebaseMessagingException exception = responses.get(i).getException();
                handleFailedToken(failedToken, exception);
            }
        }
    }

    private void handleFailedToken(String fcmToken, FirebaseMessagingException e) {
        if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED ||
            e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT) {
            List<DeviceTokenEntity> tokens = deviceTokenRepository.findAllByFcmTokenAndDelYn(fcmToken, "N");
            for (DeviceTokenEntity token : tokens) {
                token.deactivate(token.getUpdrId());
                token.softDelete(token.getUpdrId());
                deviceTokenRepository.save(token);
            }
            if (!tokens.isEmpty()) {
                log.info("유효하지 않은 토큰 비활성화 - fcmToken: {}, count: {}", fcmToken, tokens.size());
            }
        }
    }

    private boolean isFirebaseInitialized() {
        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("Firebase가 초기화되지 않았습니다. 푸시 알림을 보낼 수 없습니다.");
            return false;
        }
        return true;
    }


    private NotificationSaveRequest mappingToSaveRequest(String fcmToken, Long userId, PushMessage pushMessage) {
        return NotificationSaveRequest.builder()
                .fcmToken(fcmToken)
                .receiveUser(userId)
                .pushMessage(pushMessage)
                .successYn("Y")
                .build();
    }
    private void savePushAlarm(List<NotificationSaveRequest> saveRequestList) {
        if (saveRequestList == null || saveRequestList.isEmpty()) return;
        notificationService.saveNotification(saveRequestList);
    }
}
