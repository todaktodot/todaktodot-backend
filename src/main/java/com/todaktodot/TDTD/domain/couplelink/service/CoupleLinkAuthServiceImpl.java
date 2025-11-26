package com.todaktodot.TDTD.domain.couplelink.service;

import com.todaktodot.TDTD.domain.couplelink.dto.request.IssueLinkCodeRequestDTO;
import com.todaktodot.TDTD.domain.couplelink.dto.response.IssueLinkCodeResponseDTO;
import com.todaktodot.TDTD.domain.couplelink.repository.CoupleLinkAuthRepository;
import com.todaktodot.TDTD.domain.couplelink.repository.entity.CoupleLinkAuthEntity;
import com.todaktodot.TDTD.domain.couplelink.repository.entity.LinkStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoupleLinkAuthServiceImpl implements CoupleLinkAuthService {

    private static final String CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private static final int EXPIRY_MINUTES = 30;

    private final CoupleLinkAuthRepository coupleLinkAuthRepository;
    private final SecureRandom random = new SecureRandom();

    @Override
    @Transactional
    public IssueLinkCodeResponseDTO issueLinkCode(IssueLinkCodeRequestDTO requestDTO) {
        // TODO
        // 동일한 사용자가 기존에 생성한 만료되지 않은 코드가 있는지 검사
        // -> 있을 시, 기존 코드로 응답
        // -> 없을 시, 새로 생성(아래)

        // 1. 고유한 코드 생성
        String linkCode = generateUniqueLinkCode();

        // 2. 만료 시간 계산
        LocalDateTime expiredDt = LocalDateTime.now().plusMinutes(EXPIRY_MINUTES);

        // 3. Entity 생성 및 저장
        CoupleLinkAuthEntity entity = CoupleLinkAuthEntity.builder()
                .linkCode(linkCode)
                .issuedUserId(requestDTO.getUserId())
                .status(LinkStatus.ISSUED)
                .expiredDt(expiredDt)
                .regrId(requestDTO.getUserId())
                .updrId(requestDTO.getUserId())
                .build();

        log.info("========================================");
        log.info("생성된 링크 코드: {}", entity.getLinkCode());
        log.info("만료 시간: {}", entity.getExpiredDt());
        log.info("========================================");

        coupleLinkAuthRepository.save(entity);

        // 4. 응답 생성
        return IssueLinkCodeResponseDTO.of(linkCode, expiredDt);
    }

    /**
     * 6자리 영문(대문자) + 숫자 조합 코드 생성
     * 중복 체크하여 고유한 코드 보장
     * 최대 5번까지 재시도
     */
    private String generateUniqueLinkCode() {
        final int MAX_RETRY = 5;
        int retryCount = 0;

        while (retryCount < MAX_RETRY) {
            String code = generateRandomCode();

            if (coupleLinkAuthRepository.findByLinkCode(code).isPresent()) {
                retryCount++;
                log.warn("중복 코드 감지: {} (재시도 횟수: {}/{})", code, retryCount, MAX_RETRY);
            } else {
                if (retryCount > 0) {
                    log.info("중복 해결 완료: 최종 생성된 코드: {} (총 {}번 재시도)", code, retryCount);
                }
                return code;
            }
        }

        // 최대 재시도 횟수 초과
        log.error("링크 코드 생성 실패: 최대 재시도 횟수({})를 초과했습니다", MAX_RETRY);
        throw new IllegalStateException("링크 코드 생성에 실패했습니다. 잠시 후 다시 시도해주세요.");
    }

    private String generateRandomCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
        }
        return code.toString();
    }
}