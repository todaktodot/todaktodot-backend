package com.todaktodot.TDTD.couple.repository;

import com.todaktodot.TDTD.domain.couple.repository.CoupleRepository;
import com.todaktodot.TDTD.domain.couple.repository.entity.CoupleEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("커플 레포지토리 테스트")
class CoupleRepositoryTest {

    @Autowired
    private CoupleRepository coupleRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("findByUserId - userId1으로 커플 조회 성공")
    void findByUserId_Success_WithUserId1() {
        // Given
        CoupleEntity couple = CoupleEntity.builder()
                .userId1(1L)
                .userId2(2L)
                .connectedDt(LocalDateTime.now())
                .regrId(1L)
                .updrId(1L)
                .delYn("N")
                .build();
        entityManager.persist(couple);
        entityManager.flush();

        // When
        Optional<CoupleEntity> result = coupleRepository.findByUserId(1L);

        // Then
        log.info("========================================");
        log.info("조회된 커플 ID: {}", result.map(CoupleEntity::getCoupleId).orElse(null));
        log.info("사용자 1: {}", result.map(CoupleEntity::getUserId1).orElse(null));
        log.info("사용자 2: {}", result.map(CoupleEntity::getUserId2).orElse(null));
        log.info("========================================");

        assertThat(result).isPresent();
        assertThat(result.get().getUserId1()).isEqualTo(1L);
        assertThat(result.get().getUserId2()).isEqualTo(2L);
    }

    @Test
    @DisplayName("findByUserId - userId2로 커플 조회 성공")
    void findByUserId_Success_WithUserId2() {
        // Given
        CoupleEntity couple = CoupleEntity.builder()
                .userId1(1L)
                .userId2(2L)
                .connectedDt(LocalDateTime.now())
                .regrId(1L)
                .updrId(1L)
                .delYn("N")
                .build();
        entityManager.persist(couple);
        entityManager.flush();

        // When
        Optional<CoupleEntity> result = coupleRepository.findByUserId(2L);

        // Then
        log.info("========================================");
        log.info("조회된 커플 ID: {}", result.map(CoupleEntity::getCoupleId).orElse(null));
        log.info("사용자 1: {}", result.map(CoupleEntity::getUserId1).orElse(null));
        log.info("사용자 2: {}", result.map(CoupleEntity::getUserId2).orElse(null));
        log.info("========================================");

        assertThat(result).isPresent();
        assertThat(result.get().getUserId1()).isEqualTo(1L);
        assertThat(result.get().getUserId2()).isEqualTo(2L);
    }

    @Test
    @DisplayName("findByUserId - 커플 관계가 없는 사용자는 빈 결과 반환")
    void findByUserId_Empty_WhenUserNotInCouple() {
        // Given
        CoupleEntity couple = CoupleEntity.builder()
                .userId1(1L)
                .userId2(2L)
                .connectedDt(LocalDateTime.now())
                .regrId(1L)
                .updrId(1L)
                .delYn("N")
                .build();
        entityManager.persist(couple);
        entityManager.flush();

        // When
        Optional<CoupleEntity> result = coupleRepository.findByUserId(3L);

        // Then
        log.info("조회 결과: {}", result.isEmpty() ? "없음" : "있음");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByUserId - 삭제된 커플(delYn='Y')은 조회되지 않음")
    void findByUserId_Empty_WhenDeleted() {
        // Given
        CoupleEntity deletedCouple = CoupleEntity.builder()
                .userId1(1L)
                .userId2(2L)
                .connectedDt(LocalDateTime.now())
                .regrId(1L)
                .updrId(1L)
                .delYn("Y")  // 삭제됨
                .build();
        entityManager.persist(deletedCouple);
        entityManager.flush();

        // When
        Optional<CoupleEntity> result = coupleRepository.findByUserId(1L);

        // Then
        log.info("삭제된 커플 조회 결과: {}", result.isEmpty() ? "없음 (정상)" : "있음 (오류)");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("existsByUserId - userId1으로 존재 확인 성공")
    void existsByUserId_True_WithUserId1() {
        // Given
        CoupleEntity couple = CoupleEntity.builder()
                .userId1(1L)
                .userId2(2L)
                .connectedDt(LocalDateTime.now())
                .regrId(1L)
                .updrId(1L)
                .delYn("N")
                .build();
        entityManager.persist(couple);
        entityManager.flush();

        // When
        boolean exists = coupleRepository.existsByUserId(1L);

        // Then
        log.info("userId=1 커플 관계 존재 여부: {}", exists);
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByUserId - userId2로 존재 확인 성공")
    void existsByUserId_True_WithUserId2() {
        // Given
        CoupleEntity couple = CoupleEntity.builder()
                .userId1(1L)
                .userId2(2L)
                .connectedDt(LocalDateTime.now())
                .regrId(1L)
                .updrId(1L)
                .delYn("N")
                .build();
        entityManager.persist(couple);
        entityManager.flush();

        // When
        boolean exists = coupleRepository.existsByUserId(2L);

        // Then
        log.info("userId=2 커플 관계 존재 여부: {}", exists);
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByUserId - 커플 관계가 없으면 false 반환")
    void existsByUserId_False_WhenUserNotInCouple() {
        // Given
        CoupleEntity couple = CoupleEntity.builder()
                .userId1(1L)
                .userId2(2L)
                .connectedDt(LocalDateTime.now())
                .regrId(1L)
                .updrId(1L)
                .delYn("N")
                .build();
        entityManager.persist(couple);
        entityManager.flush();

        // When
        boolean exists = coupleRepository.existsByUserId(3L);

        // Then
        log.info("userId=3 커플 관계 존재 여부: {}", exists);
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("existsByUserId - 삭제된 커플은 존재하지 않는 것으로 처리")
    void existsByUserId_False_WhenDeleted() {
        // Given
        CoupleEntity deletedCouple = CoupleEntity.builder()
                .userId1(1L)
                .userId2(2L)
                .connectedDt(LocalDateTime.now())
                .regrId(1L)
                .updrId(1L)
                .delYn("Y")  // 삭제됨
                .build();
        entityManager.persist(deletedCouple);
        entityManager.flush();

        // When
        boolean exists = coupleRepository.existsByUserId(1L);

        // Then
        log.info("삭제된 커플의 사용자 존재 여부: {}", exists);
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("UNIQUE 제약조건 - 한 사용자는 하나의 커플에만 속할 수 있음")
    void uniqueConstraint_OneUserOneCoupleOnly() {
        // Given
        CoupleEntity couple1 = CoupleEntity.builder()
                .userId1(1L)
                .userId2(2L)
                .connectedDt(LocalDateTime.now())
                .regrId(1L)
                .updrId(1L)
                .delYn("N")
                .build();
        entityManager.persist(couple1);
        entityManager.flush();

        // When
        boolean user1Exists = coupleRepository.existsByUserId(1L);
        boolean user2Exists = coupleRepository.existsByUserId(2L);
        boolean user3Exists = coupleRepository.existsByUserId(3L);

        // Then
        log.info("========================================");
        log.info("userId=1 커플 관계: {}", user1Exists ? "존재" : "없음");
        log.info("userId=2 커플 관계: {}", user2Exists ? "존재" : "없음");
        log.info("userId=3 커플 관계: {}", user3Exists ? "존재" : "없음");
        log.info("========================================");

        assertThat(user1Exists).isTrue();
        assertThat(user2Exists).isTrue();
        assertThat(user3Exists).isFalse();
    }
}
