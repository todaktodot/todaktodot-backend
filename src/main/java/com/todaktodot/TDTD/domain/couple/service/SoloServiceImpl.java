package com.todaktodot.TDTD.domain.couple.service;

import com.todaktodot.TDTD.domain.couple.dto.response.SoloStartResponseDTO;
import com.todaktodot.TDTD.domain.couple.repository.CoupleRepository;
import com.todaktodot.TDTD.domain.couple.repository.entity.CoupleEntity;
import com.todaktodot.TDTD.domain.couple.repository.entity.CoupleType;
import com.todaktodot.TDTD.domain.dailycard.service.DailyCardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * [TDTDBE-55] 혼자 둘러보기 서비스 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SoloServiceImpl implements SoloService {

    private final CoupleRepository coupleRepository;
    private final DailyCardService dailyCardService;

    @Override
    @Transactional
    public SoloStartResponseDTO startSoloMode(Long userId) {
        log.info("[SoloService] 혼자 둘러보기 시작 요청 - userId: {}", userId);

        // 1. 이미 커플(SOLO/CONNECTED)이면 예외
        if (coupleRepository.existsByUserId(userId)) {
            throw new IllegalStateException("이미 등록된 상태입니다. 커플 연결 또는 혼자 둘러보기가 진행 중입니다.");
        }

        // 2. 1인 커플(SOLO) 생성
        CoupleEntity soloCouple = CoupleEntity.builder()
                .userId1(userId)
                .userId2(null)
                .coupleType(CoupleType.SOLO)
                .connectedDt(LocalDateTime.now())
                .regrId(userId)
                .updrId(userId)
                .build();

        coupleRepository.save(soloCouple);
        log.info("[SoloService] SOLO 커플 생성 완료 - coupleId: {}, userId: {}", soloCouple.getCoupleId(), userId);

        // 3. 오늘 날짜로 데일리카드 배정
        try {
            dailyCardService.assignMyDailyCards(userId, LocalDate.now(), LocalDate.now());
            log.info("[SoloService] 데일리카드 배정 완료 - userId: {}", userId);
        } catch (Exception e) {
            log.warn("[SoloService] 데일리카드 배정 실패 (SOLO 커플은 생성됨) - userId: {}, error: {}", userId, e.getMessage());
            // 데일리카드 배정 실패해도 SOLO 커플 생성은 유지
        }

        return SoloStartResponseDTO.from(soloCouple);
    }
}
