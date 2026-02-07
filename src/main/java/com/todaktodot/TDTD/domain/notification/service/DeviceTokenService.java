package com.todaktodot.TDTD.domain.notification.service;

import com.todaktodot.TDTD.domain.notification.dto.reqeust.DeviceTokenRequest;

public interface DeviceTokenService {

    /**
     * 디바이스 토큰 등록 또는 갱신
     * - 동일한 사용자 + 디바이스 타입이면 토큰 업데이트
     * - 새로운 경우 신규 등록
     */
    void registerOrUpdateToken(Long userId, DeviceTokenRequest request);

    /**
     * 디바이스 토큰 삭제 (로그아웃 시)
     */
    void deleteToken(Long userId, String fcmToken);
}
