package com.todaktodot.TDTD.domain.couplelink.service;

import com.todaktodot.TDTD.domain.couple.repository.CoupleRepository;
import com.todaktodot.TDTD.domain.couple.repository.entity.CoupleEntity;
import com.todaktodot.TDTD.domain.couple.repository.entity.CoupleType;
import com.todaktodot.TDTD.domain.couplelink.dto.request.ConnectLinkCodeRequestDTO;
import com.todaktodot.TDTD.domain.couplelink.dto.response.ConnectLinkCodeResponseDTO;
import com.todaktodot.TDTD.domain.couplelink.dto.response.IssueLinkCodeResponseDTO;
import com.todaktodot.TDTD.domain.couplelink.repository.CoupleLinkAuthRepository;
import com.todaktodot.TDTD.domain.couplelink.repository.entity.CoupleLinkAuthEntity;
import com.todaktodot.TDTD.domain.couplelink.repository.entity.LinkCodeStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoupleLinkAuthServiceImpl implements CoupleLinkAuthService {

    private static final String CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private static final int EXPIRY_MINUTES = 30;

    private final CoupleLinkAuthRepository coupleLinkAuthRepository;
    private final CoupleRepository coupleRepository;
    private final SecureRandom random = new SecureRandom();

    @Override
    @Transactional
    public IssueLinkCodeResponseDTO issueLinkCode(Long userId) {
        // 1. 이미 CONNECTED 커플인 사용자인지 확인 (SOLO는 코드 발급 가능)
        Optional<CoupleEntity> existingCouple = coupleRepository.findByUserId(userId);
        if (existingCouple.isPresent() && existingCouple.get().isComplete()) {
            log.warn("이미 커플인 사용자가 코드 발급 시도: {}", userId);
            throw new IllegalStateException("이미 커플인 유저입니다");
        }

        // 2. 기존에 발급받은 유효한 코드가 있는지 확인
        Optional<CoupleLinkAuthEntity> existingCode = coupleLinkAuthRepository
                .findByIssuedUserIdAndStatusAndDelYn(userId, LinkCodeStatus.ISSUED, "N");

        if (existingCode.isPresent()) {
            CoupleLinkAuthEntity existing = existingCode.get();

            // 만료되지 않은 코드면 기존 코드 반환
            if (!existing.isExpired()) {
                log.info("기존 유효한 코드 반환: {} (만료시간: {})", existing.getLinkCode(), existing.getExpiredDt());
                return IssueLinkCodeResponseDTO.of(existing.getLinkCode(), existing.getExpiredDt());
            }

            // 만료된 코드는 상태를 EXPIRED로 변경
            existing.updateStatusToExpired(userId);
            coupleLinkAuthRepository.save(existing);
            log.info("기존 만료 코드 상태 업데이트: {}", existing.getLinkCode());
        }

        // 3. 새 코드 생성
        String linkCode = generateUniqueLinkCode();
        LocalDateTime expiredDt = LocalDateTime.now().plusMinutes(EXPIRY_MINUTES);

        CoupleLinkAuthEntity entity = CoupleLinkAuthEntity.builder()
                .linkCode(linkCode)
                .issuedUserId(userId)
                .status(LinkCodeStatus.ISSUED)
                .expiredDt(expiredDt)
                .regrId(userId)
                .updrId(userId)
                .build();

        log.info("========================================");
        log.info("새 링크 코드 생성: {}", entity.getLinkCode());
        log.info("만료 시간: {}", entity.getExpiredDt());
        log.info("========================================");

        coupleLinkAuthRepository.save(entity);

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

    @Override
    @Transactional
    public ConnectLinkCodeResponseDTO connectLinkCode(Long userId, ConnectLinkCodeRequestDTO requestDTO) {
        String linkCode = requestDTO.getLinkCode();

        // 1. 코드가 존재하는지 확인
        CoupleLinkAuthEntity linkAuthEntity = coupleLinkAuthRepository.findByLinkCode(linkCode)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 링크 코드입니다"));

        Long issuedUserId = linkAuthEntity.getIssuedUserId();

        // 2. 코드가 만료되지 않았는지 확인
        if (linkAuthEntity.isExpired()) {
            log.warn("만료된 코드 사용 시도: {} (만료 시간: {})", linkCode, linkAuthEntity.getExpiredDt());
            throw new IllegalStateException("만료된 링크 코드입니다");
        }

        // 3. 코드 상태가 ISSUED인지 확인 (이미 사용된 코드는 재사용 불가)
        if (!linkAuthEntity.isIssued()) {
            log.warn("이미 사용된 코드 재사용 시도: {} (현재 상태: {})", linkCode, linkAuthEntity.getStatus());
            throw new IllegalStateException("이미 사용된 링크 코드입니다");
        }

        // 4. 자기 자신의 코드를 입력한 경우
        if (issuedUserId.equals(userId)) {
            log.warn("자기 자신의 코드 입력 시도: {}", userId);
            throw new IllegalArgumentException("자신의 링크 코드는 사용할 수 없습니다");
        }

        // 5. 발급자의 기존 커플 관계 확인 (SOLO 또는 CONNECTED)
        Optional<CoupleEntity> issuerCouple = coupleRepository.findByUserId(issuedUserId);
        if (issuerCouple.isPresent() && issuerCouple.get().isComplete()) {
            log.warn("이미 커플 관계인 사용자의 코드 사용 시도: {} (발급자: {})", linkCode, issuedUserId);
            throw new IllegalStateException("코드 발급자가 이미 커플 관계입니다");
        }

        // 6. 입력자의 기존 커플 관계 확인 (SOLO 또는 CONNECTED)
        Optional<CoupleEntity> inputCouple = coupleRepository.findByUserId(userId);
        if (inputCouple.isPresent() && inputCouple.get().isComplete()) {
            log.warn("이미 커플 관계인 사용자가 코드 입력 시도: {}", userId);
            throw new IllegalStateException("이미 커플 관계입니다");
        }

        // [TDTDBE-55] SOLO 커플 처리
        // 발급자의 SOLO 커플 조회
        Optional<CoupleEntity> issuerSoloCouple = coupleRepository.findSoloCoupleByUserId(issuedUserId);
        // 입력자의 SOLO 커플 조회
        Optional<CoupleEntity> inputSoloCouple = coupleRepository.findSoloCoupleByUserId(userId);

        CoupleEntity resultCouple;
        LocalDateTime connectedDt = LocalDateTime.now();

        if (issuerSoloCouple.isPresent()) {
            // 케이스 1, 2: 발급자가 SOLO → 발급자 커플에 입력자 추가 (발급자 데이터 유지)
            resultCouple = issuerSoloCouple.get();
            resultCouple.connectPartner(userId, userId);
            log.info("[TDTDBE-55] 발급자 SOLO 커플에 입력자 추가 - coupleId: {}", resultCouple.getCoupleId());

            // 입력자도 SOLO였다면 soft delete
            if (inputSoloCouple.isPresent()) {
                CoupleEntity inputSolo = inputSoloCouple.get();
                inputSolo.softDelete(userId);
                log.info("[TDTDBE-55] 입력자 SOLO 커플 soft delete - coupleId: {}", inputSolo.getCoupleId());
            }
        } else {
            // 케이스 3, 4: 발급자가 SOLO 아님 → 새 커플 생성 (발급자 기준)
            CoupleEntity newCouple = CoupleEntity.builder()
                    .userId1(issuedUserId)  // 코드 발급자
                    .userId2(userId)        // 코드 입력자
                    .coupleType(CoupleType.CONNECTED)
                    .connectedDt(connectedDt)
                    .regrId(userId)
                    .updrId(userId)
                    .delYn("N")
                    .build();

            resultCouple = coupleRepository.save(newCouple);
            log.info("[TDTDBE-55] 새 커플 생성 - coupleId: {}", resultCouple.getCoupleId());

            // 입력자가 SOLO였다면 soft delete
            if (inputSoloCouple.isPresent()) {
                CoupleEntity inputSolo = inputSoloCouple.get();
                inputSolo.softDelete(userId);
                log.info("[TDTDBE-55] 입력자 SOLO 커플 soft delete - coupleId: {}", inputSolo.getCoupleId());
            }
        }

        // 링크 코드 상태를 LINKED로 변경
        linkAuthEntity.linkCouple(userId, userId);
        coupleLinkAuthRepository.save(linkAuthEntity);

        log.info("========================================");
        log.info("커플 연결 성공!");
        log.info("커플 ID: {}", resultCouple.getCoupleId());
        log.info("사용자 1 (발급자): {}", issuedUserId);
        log.info("사용자 2 (입력자): {}", userId);
        log.info("연결 일시: {}", resultCouple.getConnectedDt());
        log.info("========================================");

        return ConnectLinkCodeResponseDTO.of(
                resultCouple.getCoupleId(),
                issuedUserId,
                userId,
                resultCouple.getConnectedDt()
        );
    }
}