package com.todaktodot.TDTD.domain.term.service;

import com.todaktodot.TDTD.domain.login.respository.UserRepository;
import com.todaktodot.TDTD.domain.login.respository.entity.User;
import com.todaktodot.TDTD.domain.term.dto.request.TermRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TermServiceImpl implements TermService {

    private final UserRepository userRepository;

    /**
     * 약관 동의 -> 마케팅 및 앱 알림 여부 저장
     */
    @Override
    @Transactional
    public void saveTerm(TermRequestDTO termRequestDTO) {
        Long userId = termRequestDTO.getUserId();

        if (userId == null)  {
            throw new IllegalStateException("유저ID는 필수 항목입니다.");
        }

        String infoAlarmYN = termRequestDTO.getInfoAlarmYN();
        String adAlarmYN = termRequestDTO.getAdvertiesmentAlarmYN();
        String marketingAlarmYN = termRequestDTO.getMarketingAlarmYN();

        //1. 유저 푸시알림 상태 업데이트
        User user = userRepository.findById(termRequestDTO.getUserId())
                .orElseThrow(() -> new IllegalStateException("[userID : " + userId + " ] 와 일치하는 유저가 없습니다."));

        user.updateAlarmYN(infoAlarmYN, adAlarmYN, marketingAlarmYN, userId);

        //2. 약관 동의 객체 생성 후 저장
//        Term term = Term.builder()
//                .marketingAlarmYN(marketingAndAlarmYN)
//                .regrId(userId)
//                .updrId(userId)
//                .build();
//        termRepository.save(term);

        //2. 유저 약관 동의 완료 처리
        user.updateTermYN("Y", userId);
        userRepository.save(user);
    }
}