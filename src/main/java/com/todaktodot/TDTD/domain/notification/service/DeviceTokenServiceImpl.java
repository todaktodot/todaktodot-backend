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

        // 동일 사용자 + 동일 디바이스 타입의 기존 토큰 조회
        Optional<DeviceTokenEntity> existingToken = deviceTokenRepository
                .findByUserIdAndDeviceTypeAndDelYn(userId, request.getDeviceType(), "N");

        if (existingToken.isPresent()) {
            // 기존 토큰 업데이트
            existingToken.get().updateToken(
                    request.getFcmToken(),
                    request.getOsVersion(),
                    request.getAppVersion(),
                    userId
            );
            log.info("디바이스 토큰 업데이트 - userId: {}, deviceType: {}, appVersion: {}",
                    userId, request.getDeviceType(), request.getAppVersion());
        } else {
            // 신규 토큰 등록
            DeviceTokenEntity newToken = DeviceTokenEntity.builder()
                    .user(user)
                    .fcmToken(request.getFcmToken())
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
        deviceTokenRepository.findByFcmTokenAndDelYn(fcmToken, "N")
                .ifPresent(token -> {
                    token.deactivate(userId);
                    log.info("디바이스 토큰 비활성화 - userId: {}, fcmToken: {}", userId, fcmToken);
                });
    }
}
