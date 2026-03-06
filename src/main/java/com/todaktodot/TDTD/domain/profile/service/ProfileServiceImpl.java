package com.todaktodot.TDTD.domain.profile.service;

import com.todaktodot.TDTD.domain.couple.repository.CoupleRepository;
import com.todaktodot.TDTD.domain.couple.repository.entity.CoupleEntity;
import com.todaktodot.TDTD.domain.couple.repository.entity.CoupleType;
import com.todaktodot.TDTD.domain.login.respository.UserRepository;
import com.todaktodot.TDTD.domain.login.respository.entity.User;
import com.todaktodot.TDTD.domain.login.respository.entity.UserAccount;
import com.todaktodot.TDTD.domain.notification.repository.DeviceTokenRepository;
import com.todaktodot.TDTD.domain.notification.repository.entity.DeviceTokenEntity;
import com.todaktodot.TDTD.domain.profile.dto.request.SetNicknameRequestDTO;
import com.todaktodot.TDTD.domain.profile.dto.response.SetNicknameResponseDTO;
import com.todaktodot.TDTD.domain.profile.dto.response.UserDetailResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final CoupleRepository coupleRepository;

    @Override
    @Transactional
    public SetNicknameResponseDTO setNickname(Long userId, SetNicknameRequestDTO requestDTO) {
        User user = userRepository.findByIdAndDelYn(userId, "N")
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        user.updateNickname(requestDTO.getNickname());

        return SetNicknameResponseDTO.of(user.getId(), user.getNickname());
    }

    /**
     * 회원정보 조회
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetailResponseDTO getDetail(long userId) {
        //로그인한 사용자
        User loginUser = userRepository.findByIdAndDelYn(userId, "N")
                .orElseThrow(() -> new IllegalArgumentException("[userID : " + userId +" ] 사용자를 찾을 수 없습니다"));

        //커플정보 존재 시
        if (coupleRepository.existsByUserId(userId)) {
            CoupleEntity couple = coupleRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("[userID : " + userId +" ] 커플정보를 찾을 수 없습니다"));

            Long userId1 = couple.getUserId1();
            Long userId2 = couple.getUserId2();

            //커플인 경우
            if (couple.isComplete()) {
                User anotherUser;
                if (userId1 == userId) {
                    anotherUser = userRepository.findByIdAndDelYn(userId2, "N")
                            .orElseThrow(() -> new IllegalArgumentException("[userID : " + userId2 + " ] 사용자를 찾을 수 없습니다"));
                } else {
                    anotherUser = userRepository.findByIdAndDelYn(userId1, "N")
                            .orElseThrow(() -> new IllegalArgumentException("[userID : " + userId1 + " ] 사용자를 찾을 수 없습니다"));
                }

                //우리가 만난 기간 계산
                String sinceMetDt = null;
                if (couple.getFirstMetDt() != null) {
                    sinceMetDt = getString(couple.getFirstMetDt());
                }

                //1. 커플정보 생성
                UserDetailResponseDTO.CoupleDetail coupleDetail = UserDetailResponseDTO.CoupleDetail.builder()
                        .coupleId(couple.getCoupleId())
                        .loginUserId(loginUser.getId())
                        .loginNickname(loginUser.getNickname())
                        .anotherUserId(anotherUser.getId())
                        .anotherNickname(anotherUser.getNickname())
                        .firstMetDt(couple.getFirstMetDt())
                        .sinceMetDt(sinceMetDt)
                        .relationshipStage(couple.getRelationshipStage() == null ? null : couple.getRelationshipStage().name())
                        .connectedDt(couple.getConnectedDt())
                        .delYn(couple.getDelYn())
                        .build();

                //2. 로그인한 유저 정보 생성
                UserDetailResponseDTO userDetailResponseDTO = UserDetailResponseDTO.of(loginUser);
                userDetailResponseDTO.setIsCouple("Y");
                userDetailResponseDTO.setCoupleType(CoupleType.CONNECTED.name());
                userDetailResponseDTO.setCoupleDetailInfo(coupleDetail);
                return userDetailResponseDTO;
            }
            //혼자 둘러보는 경우
            else {
                UserDetailResponseDTO userDetailResponseDTO = UserDetailResponseDTO.of(loginUser);
                userDetailResponseDTO.setIsCouple("N");
                userDetailResponseDTO.setCoupleType(CoupleType.SOLO.name());
                return userDetailResponseDTO;
            }
        }
        //커플도 아니고, 혼자 둘러보기도 아닌 경우
        else {
            UserDetailResponseDTO userDetailResponseDTO = UserDetailResponseDTO.of(loginUser);
            userDetailResponseDTO.setIsCouple("N");
            return userDetailResponseDTO;
        }
    }

    /**
     * 회원탈퇴
     */
    @Override
    @Transactional
    public void withdraw(long userId) {
        User loginUser = userRepository.findByIdAndDelYn(userId, "N")
                .orElseThrow(() -> new IllegalArgumentException("[userID : " + userId +" ] 사용자를 찾을 수 없습니다"));

        //계쩡 정보 삭제
        List<UserAccount> socialAccounts = loginUser.getSocialAccounts();
        socialAccounts.forEach(acc -> {
            acc.softDelete(userId);
        });

        //회원 정보 삭제
        loginUser.softDelete(userId);

        //디바이스 토큰 삭제
        List<DeviceTokenEntity> allToken = deviceTokenRepository.findAllByUserId(userId);
        allToken.forEach(at -> {
            at.softDelete(userId);
        });

        //커플 해지
        CoupleEntity couple = coupleRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("커플 관계가 존재하지 않습니다"));

        // DEL_YN = 'Y' 처리
        couple.disconnect(userId);
        coupleRepository.save(couple);
    }

    /**
     * 만난 날 계산
     */
    private static String getString(LocalDate firstMetDt) {
        LocalDate now = LocalDate.now();
        Period period = Period.between(firstMetDt, now);
        String year = period.getYears() + "년 ";
        String month = period.getMonths() + "개월 ";
        String day = period.getDays() + "일";

        if (period.getYears() == 0) {
            if (period.getMonths() == 0) {
                return day;
            }
            return month + day;
        }
        else {
            if (period.getMonths() == 0) {
                return year + day;
            }
            return year + month + day;
        }
    }
}
