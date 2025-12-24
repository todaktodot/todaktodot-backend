package com.todaktodot.TDTD.couplelink.service;

import com.todaktodot.TDTD.domain.couple.repository.CoupleRepository;
import com.todaktodot.TDTD.domain.couplelink.dto.response.IssueLinkCodeResponseDTO;
import com.todaktodot.TDTD.domain.couplelink.repository.CoupleLinkAuthRepository;
import com.todaktodot.TDTD.domain.couplelink.repository.entity.CoupleLinkAuthEntity;
import com.todaktodot.TDTD.domain.couplelink.repository.entity.LinkCodeStatus;
import com.todaktodot.TDTD.domain.couplelink.service.CoupleLinkAuthServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("커플 연결 코드 발급 서비스 테스트")
class CoupleLinkAuthServiceImplTest {

    @Mock
    private CoupleLinkAuthRepository coupleLinkAuthRepository;

    @Mock
    private CoupleRepository coupleRepository;

    @InjectMocks
    private CoupleLinkAuthServiceImpl coupleLinkAuthService;

    @Test
    @DisplayName("코드 발급 성공 - 6자리 영문+숫자 코드 생성")
    void issueLinkCode_Success() {
        // Given
        Long userId = 1L;

        // 중복 체크 시 항상 코드가 없다고 가정
        when(coupleLinkAuthRepository.findByLinkCode(anyString()))
                .thenReturn(Optional.empty());

        when(coupleLinkAuthRepository.save(any(CoupleLinkAuthEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        IssueLinkCodeResponseDTO response = coupleLinkAuthService.issueLinkCode(userId);

        // 생성된 응답 로그 출력
        log.info("========================================");
        log.info("생성된 링크 코드: {}", response.getLinkCode());
        log.info("만료 시간: {}", response.getExpiredDt());
        log.info("========================================");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getLinkCode()).isNotNull();
        assertThat(response.getLinkCode()).hasSize(6);
        assertThat(response.getLinkCode()).matches("^[A-Z0-9]{6}$");
        assertThat(response.getExpiredDt()).isAfter(LocalDateTime.now());

        // Repository save 메서드가 호출되었는지 확인
        verify(coupleLinkAuthRepository, times(1)).save(any(CoupleLinkAuthEntity.class));
    }

    @Test
    @DisplayName("중복 코드 발생 시 재생성")
    void issueLinkCode_DuplicateCodeRetry() {
        // Given
        Long userId = 1L;

        CoupleLinkAuthEntity existingEntity = CoupleLinkAuthEntity.builder()
                .linkCode("ABC123")
                .issuedUserId(1L)
                .status(LinkCodeStatus.ISSUED)
                .expiredDt(LocalDateTime.now().plusMinutes(30))
                .regrId(1L)
                .updrId(1L)
                .build();

        // 첫 번째 호출: 중복 존재, 두 번째 호출: 중복 없음
        when(coupleLinkAuthRepository.findByLinkCode(anyString()))
                .thenReturn(Optional.of(existingEntity))
                .thenReturn(Optional.empty());

        when(coupleLinkAuthRepository.save(any(CoupleLinkAuthEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        IssueLinkCodeResponseDTO response = coupleLinkAuthService.issueLinkCode(userId);

        // 중복 체크 후 생성된 응답 로그 출력
        log.info("========================================");
        log.info("중복 코드 재생성 테스트");
        log.info("최종 생성된 링크 코드: {}", response.getLinkCode());
        log.info("만료 시간: {}", response.getExpiredDt());
        log.info("========================================");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getLinkCode()).isNotNull();

        // findByLinkCode가 최소 2번 호출되었는지 확인 (중복 체크 + 재시도)
        verify(coupleLinkAuthRepository, atLeast(2)).findByLinkCode(anyString());
    }
}