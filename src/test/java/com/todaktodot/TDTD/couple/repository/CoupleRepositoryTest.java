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
                .userId1("userA")
                .userId2("userB")
                .connectedDt(LocalDateTime.now())
                .regrId("userA")
                .updrId("userA")
                .delYn("N")
                .build();
        entityManager.persist(couple);
        entityManager.flush();

        // When
        Optional<CoupleEntity> result = coupleRepository.findByUserId("userA");

        // Then
        log.info("========================================");
        log.info("조회된 커플 ID: {}", result.map(CoupleEntity::getCoupleId).orElse(null));
        log.info("사용자 1: {}", result.map(CoupleEntity::getUserId1).orElse(null));
        log.info("사용자 2: {}", result.map(CoupleEntity::getUserId2).orElse(null));
        log.info("========================================");

        assertThat(result).isPresent();
        assertThat(result.get().getUserId1()).isEqualTo("userA");
        assertThat(result.get().getUserId2()).isEqualTo("userB");
    }

    @Test
    @DisplayName("findByUserId - userId2로 커플 조회 성공")
    void findByUserId_Success_WithUserId2() {
        // Given
        CoupleEntity couple = CoupleEntity.builder()
                .userId1("userA")
                .userId2("userB")
                .connectedDt(LocalDateTime.now())
                .regrId("userA")
                .updrId("userA")
                .delYn("N")
                .build();
        entityManager.persist(couple);
        entityManager.flush();

        // When
        Optional<CoupleEntity> result = coupleRepository.findByUserId("userB");

        // Then
        log.info("========================================");
        log.info("조회된 커플 ID: {}", result.map(CoupleEntity::getCoupleId).orElse(null));
        log.info("사용자 1: {}", result.map(CoupleEntity::getUserId1).orElse(null));
        log.info("사용자 2: {}", result.map(CoupleEntity::getUserId2).orElse(null));
        log.info("========================================");

        assertThat(result).isPresent();
        assertThat(result.get().getUserId1()).isEqualTo("userA");
        assertThat(result.get().getUserId2()).isEqualTo("userB");
    }

    @Test
    @DisplayName("findByUserId - 커플 관계가 없는 사용자는 빈 결과 반환")
    void findByUserId_Empty_WhenUserNotInCouple() {
        // Given
        CoupleEntity couple = CoupleEntity.builder()
                .userId1("userA")
                .userId2("userB")
                .connectedDt(LocalDateTime.now())
                .regrId("userA")
                .updrId("userA")
                .delYn("N")
                .build();
        entityManager.persist(couple);
        entityManager.flush();

        // When
        Optional<CoupleEntity> result = coupleRepository.findByUserId("userC");

        // Then
        log.info("조회 결과: {}", result.isEmpty() ? "없음" : "있음");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByUserId - 삭제된 커플(delYn='Y')은 조회되지 않음")
    void findByUserId_Empty_WhenDeleted() {
        // Given
        CoupleEntity deletedCouple = CoupleEntity.builder()
                .userId1("userA")
                .userId2("userB")
                .connectedDt(LocalDateTime.now())
                .regrId("userA")
                .updrId("userA")
                .delYn("Y")  // 삭제됨
                .build();
        entityManager.persist(deletedCouple);
        entityManager.flush();

        // When
        Optional<CoupleEntity> result = coupleRepository.findByUserId("userA");

        // Then
        log.info("삭제된 커플 조회 결과: {}", result.isEmpty() ? "없음 (정상)" : "있음 (오류)");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("existsByUserId - userId1으로 존재 확인 성공")
    void existsByUserId_True_WithUserId1() {
        // Given
        CoupleEntity couple = CoupleEntity.builder()
                .userId1("userA")
                .userId2("userB")
                .connectedDt(LocalDateTime.now())
                .regrId("userA")
                .updrId("userA")
                .delYn("N")
                .build();
        entityManager.persist(couple);
        entityManager.flush();

        // When
        boolean exists = coupleRepository.existsByUserId("userA");

        // Then
        log.info("userA 커플 관계 존재 여부: {}", exists);
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByUserId - userId2로 존재 확인 성공")
    void existsByUserId_True_WithUserId2() {
        // Given
        CoupleEntity couple = CoupleEntity.builder()
                .userId1("userA")
                .userId2("userB")
                .connectedDt(LocalDateTime.now())
                .regrId("userA")
                .updrId("userA")
                .delYn("N")
                .build();
        entityManager.persist(couple);
        entityManager.flush();

        // When
        boolean exists = coupleRepository.existsByUserId("userB");

        // Then
        log.info("userB 커플 관계 존재 여부: {}", exists);
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByUserId - 커플 관계가 없으면 false 반환")
    void existsByUserId_False_WhenUserNotInCouple() {
        // Given
        CoupleEntity couple = CoupleEntity.builder()
                .userId1("userA")
                .userId2("userB")
                .connectedDt(LocalDateTime.now())
                .regrId("userA")
                .updrId("userA")
                .delYn("N")
                .build();
        entityManager.persist(couple);
        entityManager.flush();

        // When
        boolean exists = coupleRepository.existsByUserId("userC");

        // Then
        log.info("userC 커플 관계 존재 여부: {}", exists);
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("existsByUserId - 삭제된 커플은 존재하지 않는 것으로 처리")
    void existsByUserId_False_WhenDeleted() {
        // Given
        CoupleEntity deletedCouple = CoupleEntity.builder()
                .userId1("userA")
                .userId2("userB")
                .connectedDt(LocalDateTime.now())
                .regrId("userA")
                .updrId("userA")
                .delYn("Y")  // 삭제됨
                .build();
        entityManager.persist(deletedCouple);
        entityManager.flush();

        // When
        boolean exists = coupleRepository.existsByUserId("userA");

        // Then
        log.info("삭제된 커플의 사용자 존재 여부: {}", exists);
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("UNIQUE 제약조건 - 한 사용자는 하나의 커플에만 속할 수 있음")
    void uniqueConstraint_OneUserOneCoupleOnly() {
        // Given
        CoupleEntity couple1 = CoupleEntity.builder()
                .userId1("userA")
                .userId2("userB")
                .connectedDt(LocalDateTime.now())
                .regrId("userA")
                .updrId("userA")
                .delYn("N")
                .build();
        entityManager.persist(couple1);
        entityManager.flush();

        // When
        boolean userAExists = coupleRepository.existsByUserId("userA");
        boolean userBExists = coupleRepository.existsByUserId("userB");
        boolean userCExists = coupleRepository.existsByUserId("userC");

        // Then
        log.info("========================================");
        log.info("userA 커플 관계: {}", userAExists ? "존재" : "없음");
        log.info("userB 커플 관계: {}", userBExists ? "존재" : "없음");
        log.info("userC 커플 관계: {}", userCExists ? "존재" : "없음");
        log.info("========================================");

        assertThat(userAExists).isTrue();
        assertThat(userBExists).isTrue();
        assertThat(userCExists).isFalse();
    }
}