package com.todaktodot.TDTD.domain.couple.service;

import com.todaktodot.TDTD.domain.couple.dto.response.SoloStartResponseDTO;

/**
 * [TDTDBE-55] 혼자 둘러보기 서비스
 */
public interface SoloService {

    /**
     * 혼자 둘러보기 시작
     * - 1인 커플(SOLO) 생성
     * - 오늘 날짜로 데일리카드 배정
     *
     * @param userId 사용자 ID
     * @return 생성된 SOLO 커플 정보
     */
    SoloStartResponseDTO startSoloMode(Long userId);
}
