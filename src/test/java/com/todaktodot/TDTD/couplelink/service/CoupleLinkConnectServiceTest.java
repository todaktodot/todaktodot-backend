package com.todaktodot.TDTD.couplelink.service;

import com.todaktodot.TDTD.domain.couple.repository.CoupleRepository;
import com.todaktodot.TDTD.domain.couple.repository.entity.CoupleEntity;
import com.todaktodot.TDTD.domain.couplelink.dto.request.ConnectLinkCodeRequestDTO;
import com.todaktodot.TDTD.domain.couplelink.dto.response.ConnectLinkCodeResponseDTO;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("커플 연결 서비스 테스트")
class CoupleLinkConnectServiceTest {

    @Mock
    private CoupleLinkAuthRepository coupleLinkAuthRepository;

    @Mock
    private CoupleRepository coupleRepository;

    @InjectMocks
    private CoupleLinkAuthServiceImpl coupleLinkAuthService;

    @Test
    @DisplayName("커플 연결 성공 - 모든 검증 통과")
    void connectLinkCode_Success() {
        // Given
        Long issuedUserId = 1L;
        Long inputUserId = 2L;
        String linkCode = "ABC123";

        ConnectLinkCodeRequestDTO request = new ConnectLinkCodeRequestDTO(linkCode);

        CoupleLinkAuthEntity linkAuthEntity = CoupleLinkAuthEntity.builder()
                .linkCode(linkCode)
                .issuedUserId(issuedUserId)
                .status(LinkCodeStatus.ISSUED)
                .expiredDt(LocalDateTime.now().plusMinutes(30))
                .regrId(issuedUserId)
                .updrId(issuedUserId)
                .build();

        CoupleEntity savedCouple = CoupleEntity.builder()
                .coupleId(1L)
                .userId1(issuedUserId)
                .userId2(inputUserId)
                .connectedDt(LocalDateTime.now())
                .regrId(inputUserId)
                .updrId(inputUserId)
                .build();

        when(coupleLinkAuthRepository.findByLinkCode(linkCode))
                .thenReturn(Optional.of(linkAuthEntity));
        when(coupleRepository.existsByUserId(issuedUserId)).thenReturn(false);
        when(coupleRepository.existsByUserId(inputUserId)).thenReturn(false);
        when(coupleRepository.save(any(CoupleEntity.class)))
                .thenReturn(savedCouple);
        when(coupleLinkAuthRepository.save(any(CoupleLinkAuthEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ConnectLinkCodeResponseDTO response = coupleLinkAuthService.connectLinkCode(inputUserId, request);

        // Then
        log.info("========================================");
        log.info("커플 ID: {}", response.getCoupleId());
        log.info("사용자 1: {}", response.getUserId1());
        log.info("사용자 2: {}", response.getUserId2());
        log.info("연결 일시: {}", response.getConnectedDt());
        log.info("========================================");

        assertThat(response).isNotNull();
        assertThat(response.getCoupleId()).isEqualTo(1L);
        assertThat(response.getUserId1()).isEqualTo(issuedUserId);
        assertThat(response.getUserId2()).isEqualTo(inputUserId);
        assertThat(response.getConnectedDt()).isNotNull();

        // Couple 저장 검증
        verify(coupleRepository, times(1)).save(any(CoupleEntity.class));
        // LinkAuth 상태 업데이트 검증
        verify(coupleLinkAuthRepository, times(1)).save(any(CoupleLinkAuthEntity.class));
    }

    @Test
    @DisplayName("커플 연결 실패 - 존재하지 않는 코드")
    void connectLinkCode_Fail_CodeNotFound() {
        // Given
        Long inputUserId = 2L;
        String linkCode = "NOTEXIST";
        ConnectLinkCodeRequestDTO request = new ConnectLinkCodeRequestDTO(linkCode);

        when(coupleLinkAuthRepository.findByLinkCode(linkCode))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> coupleLinkAuthService.connectLinkCode(inputUserId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 링크 코드입니다");

        verify(coupleRepository, never()).save(any());
    }

    @Test
    @DisplayName("커플 연결 실패 - 만료된 코드")
    void connectLinkCode_Fail_ExpiredCode() {
        // Given
        Long inputUserId = 2L;
        String linkCode = "EXPIRED1";
        ConnectLinkCodeRequestDTO request = new ConnectLinkCodeRequestDTO(linkCode);

        CoupleLinkAuthEntity expiredEntity = CoupleLinkAuthEntity.builder()
                .linkCode(linkCode)
                .issuedUserId(1L)
                .status(LinkCodeStatus.ISSUED)
                .expiredDt(LocalDateTime.now().minusMinutes(10))  // 10분 전 만료
                .regrId(1L)
                .updrId(1L)
                .build();

        when(coupleLinkAuthRepository.findByLinkCode(linkCode))
                .thenReturn(Optional.of(expiredEntity));

        // When & Then
        assertThatThrownBy(() -> coupleLinkAuthService.connectLinkCode(inputUserId, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("만료된 링크 코드입니다");

        verify(coupleRepository, never()).save(any());
    }

    @Test
    @DisplayName("커플 연결 실패 - 이미 사용된 코드 (LINKED 상태)")
    void connectLinkCode_Fail_AlreadyUsedCode() {
        // Given
        Long inputUserId = 2L;
        String linkCode = "USED123";
        ConnectLinkCodeRequestDTO request = new ConnectLinkCodeRequestDTO(linkCode);

        CoupleLinkAuthEntity linkedEntity = CoupleLinkAuthEntity.builder()
                .linkCode(linkCode)
                .issuedUserId(1L)
                .status(LinkCodeStatus.ISSUED)
                .expiredDt(LocalDateTime.now().plusMinutes(30))
                .regrId(1L)
                .updrId(1L)
                .build();

        // 이미 사용된 상태로 변경
        linkedEntity.linkCouple(3L, 1L);

        when(coupleLinkAuthRepository.findByLinkCode(linkCode))
                .thenReturn(Optional.of(linkedEntity));

        // When & Then
        assertThatThrownBy(() -> coupleLinkAuthService.connectLinkCode(inputUserId, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 사용된 링크 코드입니다");

        verify(coupleRepository, never()).save(any());
    }

    @Test
    @DisplayName("커플 연결 실패 - 자기 자신의 코드 입력")
    void connectLinkCode_Fail_SelfCode() {
        // Given
        Long userId = 1L;
        String linkCode = "SELF123";
        ConnectLinkCodeRequestDTO request = new ConnectLinkCodeRequestDTO(linkCode);

        CoupleLinkAuthEntity linkAuthEntity = CoupleLinkAuthEntity.builder()
                .linkCode(linkCode)
                .issuedUserId(userId)  // 발급자와 입력자가 같음
                .status(LinkCodeStatus.ISSUED)
                .expiredDt(LocalDateTime.now().plusMinutes(30))
                .regrId(userId)
                .updrId(userId)
                .build();

        when(coupleLinkAuthRepository.findByLinkCode(linkCode))
                .thenReturn(Optional.of(linkAuthEntity));

        // When & Then
        assertThatThrownBy(() -> coupleLinkAuthService.connectLinkCode(userId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("자신의 링크 코드는 사용할 수 없습니다");

        verify(coupleRepository, never()).save(any());
    }

    @Test
    @DisplayName("커플 연결 실패 - 발급자가 이미 커플 관계")
    void connectLinkCode_Fail_IssuerAlreadyCoupled() {
        // Given
        Long issuedUserId = 1L;
        Long inputUserId = 2L;
        String linkCode = "ABC123";
        ConnectLinkCodeRequestDTO request = new ConnectLinkCodeRequestDTO(linkCode);

        CoupleLinkAuthEntity linkAuthEntity = CoupleLinkAuthEntity.builder()
                .linkCode(linkCode)
                .issuedUserId(issuedUserId)
                .status(LinkCodeStatus.ISSUED)
                .expiredDt(LocalDateTime.now().plusMinutes(30))
                .regrId(issuedUserId)
                .updrId(issuedUserId)
                .build();

        when(coupleLinkAuthRepository.findByLinkCode(linkCode))
                .thenReturn(Optional.of(linkAuthEntity));
        when(coupleRepository.existsByUserId(issuedUserId))
                .thenReturn(true);  // 발급자가 이미 커플

        // When & Then
        assertThatThrownBy(() -> coupleLinkAuthService.connectLinkCode(inputUserId, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("코드 발급자가 이미 커플 관계입니다");

        verify(coupleRepository, never()).save(any());
    }

    @Test
    @DisplayName("커플 연결 실패 - 입력자가 이미 커플 관계")
    void connectLinkCode_Fail_InputUserAlreadyCoupled() {
        // Given
        Long issuedUserId = 1L;
        Long inputUserId = 2L;
        String linkCode = "ABC123";
        ConnectLinkCodeRequestDTO request = new ConnectLinkCodeRequestDTO(linkCode);

        CoupleLinkAuthEntity linkAuthEntity = CoupleLinkAuthEntity.builder()
                .linkCode(linkCode)
                .issuedUserId(issuedUserId)
                .status(LinkCodeStatus.ISSUED)
                .expiredDt(LocalDateTime.now().plusMinutes(30))
                .regrId(issuedUserId)
                .updrId(issuedUserId)
                .build();

        when(coupleLinkAuthRepository.findByLinkCode(linkCode))
                .thenReturn(Optional.of(linkAuthEntity));
        when(coupleRepository.existsByUserId(issuedUserId)).thenReturn(false);
        when(coupleRepository.existsByUserId(inputUserId))
                .thenReturn(true);  // 입력자가 이미 커플

        // When & Then
        assertThatThrownBy(() -> coupleLinkAuthService.connectLinkCode(inputUserId, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 커플 관계입니다");

        verify(coupleRepository, never()).save(any());
    }
}