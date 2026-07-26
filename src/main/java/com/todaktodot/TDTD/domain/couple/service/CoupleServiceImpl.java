package com.todaktodot.TDTD.domain.couple.service;

import com.todaktodot.TDTD.domain.couple.dto.request.UpdateCoupleInfoRequestDTO;
import com.todaktodot.TDTD.domain.couple.dto.response.CoupleInfoResponseDTO;
import com.todaktodot.TDTD.domain.couple.repository.CoupleRepository;
import com.todaktodot.TDTD.domain.couple.repository.entity.CoupleEntity;
import com.todaktodot.TDTD.domain.login.respository.UserRepository;
import com.todaktodot.TDTD.domain.login.respository.entity.User;
import com.todaktodot.TDTD.domain.notification.dto.PushMessage;
import com.todaktodot.TDTD.domain.notification.service.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoupleServiceImpl implements CoupleService {

    private final CoupleRepository coupleRepository;
    private final UserRepository userRepository;
    private final FcmService fcmService;

    @Override
    @Transactional
    public CoupleInfoResponseDTO updateCoupleInfo(Long userId, UpdateCoupleInfoRequestDTO requestDTO) {
        // 1. 사용자가 속한 커플 조회
        CoupleEntity couple = coupleRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("커플 관계가 존재하지 않습니다"));

        // 2. 커플 정보 업데이트
        couple.updateCoupleInfo(
                requestDTO.getFirstMetDt(),
                requestDTO.getRelationshipStage(),
                userId
        );

        // 3. 저장 및 응답
        CoupleEntity savedCouple = coupleRepository.save(couple);

        log.info("========================================");
        log.info("커플 정보 업데이트 완료");
        log.info("커플 ID: {}", savedCouple.getCoupleId());
        log.info("우리가 만난 날: {}", savedCouple.getFirstMetDt());
        log.info("관계 단계: {}", savedCouple.getRelationshipStage());
        log.info("========================================");

        return CoupleInfoResponseDTO.from(savedCouple);
    }

    @Override
    @Transactional(readOnly = true)
    public CoupleInfoResponseDTO getCoupleInfo(Long userId) {
        CoupleEntity couple = coupleRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("커플 관계가 존재하지 않습니다"));

        return CoupleInfoResponseDTO.from(couple);
    }

    @Override
    @Transactional
    public void disconnectCouple(Long userId) {
        // 1. 사용자가 속한 커플 조회
        CoupleEntity couple = coupleRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("커플 관계가 존재하지 않습니다"));

        // 2. DEL_YN = 'Y' 처리
        couple.disconnect(userId);
        coupleRepository.save(couple);

        //3. 닉네임 초기화
        Long userId1 = couple.getUserId1();
        Long userId2 = couple.getUserId2();

        User firstUser = userRepository.findByIdAndDelYn(userId1, "N")
                .orElseThrow(() -> new IllegalStateException(userId1 + " 사용자가 존재하지 않습니다."));
        firstUser.nicknameClear(userId1);

        User secondUser = userRepository.findByIdAndDelYn(userId2, "N")
                .orElseThrow(() -> new IllegalStateException(userId2 + " 사용자가 존재하지 않습니다."));
        secondUser.nicknameClear(userId2);

        userRepository.save(firstUser);
        userRepository.save(secondUser);

        //4. 커플 해제 시 사일런트 알림 발송
        Long anotherUserId = (userId1.equals(userId)) ? userId2 : userId1;
        disconnectCouplePushAlarm(anotherUserId, couple.getCoupleId());

        log.info("========================================");
        log.info("커플 해지 완료");
        log.info("커플 ID: {}", couple.getCoupleId());
        log.info("해지 요청자: {}", userId);
        log.info("========================================");
    }

    private void disconnectCouplePushAlarm(Long receiveUserId, Long coupleId) {
        PushMessage pushMessage = PushMessage.disconnectCouple(coupleId);
        fcmService.sendToUser(receiveUserId, pushMessage);
    }
}
