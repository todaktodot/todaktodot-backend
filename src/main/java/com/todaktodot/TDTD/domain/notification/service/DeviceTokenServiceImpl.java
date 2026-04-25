package com.todaktodot.TDTD.domain.notification.service;

import com.todaktodot.TDTD.domain.login.respository.UserRepository;
import com.todaktodot.TDTD.domain.login.respository.entity.User;
import com.todaktodot.TDTD.domain.notification.dto.reqeust.DeviceTokenRequest;
import com.todaktodot.TDTD.domain.notification.repository.DeviceTokenRepository;
import com.todaktodot.TDTD.domain.notification.repository.entity.DeviceTokenEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceTokenServiceImpl implements DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void registerOrUpdateToken(Long userId, DeviceTokenRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        String fcmToken = request.getFcmToken();

        // 같은 토큰이 다른 계정/슬롯에 남아 있으면 먼저 정리
        List<DeviceTokenEntity> sameTokenRows = deviceTokenRepository.findAllByFcmTokenAndDelYn(fcmToken, "N");
        for (DeviceTokenEntity tokenRow : sameTokenRows) {
            boolean sameUser = tokenRow.getUser().getId().equals(userId);
            boolean sameDeviceType = tokenRow.getDeviceType() == request.getDeviceType();

            if (sameUser && sameDeviceType) {
                continue;
            }

            if (Boolean.TRUE.equals(tokenRow.getIsActive())) {
                tokenRow.deactivate(userId);
            }
            tokenRow.softDelete(userId);
            log.info("중복 FCM 토큰 정리 - ownerUserId: {}, newUserId: {}, deviceType: {}",
                    tokenRow.getUser().getId(), userId, tokenRow.getDeviceType());
        }

        // 동일 사용자 + 동일 디바이스 타입의 기존 활성/미삭제 토큰 조회
        List<DeviceTokenEntity> sameUserDeviceRows = deviceTokenRepository
                .findAllByUserIdAndDeviceTypeAndDelYn(userId, request.getDeviceType(), "N");

        DeviceTokenEntity primaryToken = sameUserDeviceRows.isEmpty() ? null : sameUserDeviceRows.get(0);

        // 같은 사용자/디바이스 슬롯에 중복 row가 있으면 정리
        if (sameUserDeviceRows.size() > 1) {
            for (int i = 1; i < sameUserDeviceRows.size(); i++) {
                DeviceTokenEntity extraToken = sameUserDeviceRows.get(i);
                if (Boolean.TRUE.equals(extraToken.getIsActive())) {
                    extraToken.deactivate(userId);
                }
                extraToken.softDelete(userId);
                log.info("동일 사용자/디바이스 타입 중복 토큰 정리 - userId: {}, deviceType: {}, deviceTokenSeq: {}",
                        userId, request.getDeviceType(), extraToken.getDeviceTokenSeq());
            }
        }

        if (primaryToken != null) {
            primaryToken.updateToken(
                    fcmToken,
                    request.getOsVersion(),
                    request.getAppVersion(),
                    userId
            );
            log.info("디바이스 토큰 업데이트 - userId: {}, deviceType: {}, appVersion: {}",
                    userId, request.getDeviceType(), request.getAppVersion());
        } else {
            DeviceTokenEntity newToken = DeviceTokenEntity.builder()
                    .user(user)
                    .fcmToken(fcmToken)
                    .deviceType(request.getDeviceType())
                    .osVersion(request.getOsVersion())
                    .appVersion(request.getAppVersion())
                    .regrId(userId)
                    .updrId(userId)
                    .build();
            deviceTokenRepository.save(newToken);
            log.info("디바이스 토큰 신규 등록 - userId: {}, deviceType: {}, appVersion: {}",
                    userId, request.getDeviceType(), request.getAppVersion());
        }
    }

    @Override
    @Transactional
    public void deleteToken(Long userId, String fcmToken) {
        List<DeviceTokenEntity> tokens = deviceTokenRepository.findAllByFcmTokenAndDelYn(fcmToken, "N");
        tokens.stream()
                .filter(token -> token.getUser().getId().equals(userId))
                .forEach(token -> {
                    token.deactivate(userId);
                    token.softDelete(userId);
                    log.info("디바이스 토큰 비활성화 - userId: {}, fcmToken: {}, deviceTokenSeq: {}",
                            userId, fcmToken, token.getDeviceTokenSeq());
                });
    }
}
