package com.todaktodot.TDTD.domain.term.service;

import com.todaktodot.TDTD.domain.login.respository.UserRepository;
import com.todaktodot.TDTD.domain.login.respository.entity.User;
import com.todaktodot.TDTD.domain.term.dto.request.TermRequestDTO;
import com.todaktodot.TDTD.domain.term.repository.TermRepository;
import com.todaktodot.TDTD.domain.term.repository.entity.Term;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TermServiceImpl implements TermService {

    private final TermRepository termRepository;
    private final UserRepository userRepository;

    /**
     * 약관 동의 -> 마케팅 및 앱 알림 여부 저장
     */
    @Override
    @Transactional
    public void saveTerm(TermRequestDTO termRequestDTO) {
        Long userId = termRequestDTO.getUserId();
        String marketingAndAlarmYN = termRequestDTO.getMarketingAndAlarmYN();

        if (userId == null || marketingAndAlarmYN == null) return;

        //1. 유저 푸시알림 상태 업데이트
        User user = userRepository.findById(termRequestDTO.getUserId()).orElseThrow();
        user.updateAlarmYN(marketingAndAlarmYN, userId);

        //2. 약관 동의 객체 생성 후 저장
        Term term = Term.builder()
                .marketingAlarmYN(marketingAndAlarmYN)
                .regrId(userId)
                .updrId(userId)
                .build();
        termRepository.save(term);

        //3. 유저 가입처리
        user.updateJoinYN("Y", userId);
        userRepository.save(user);
    }
}