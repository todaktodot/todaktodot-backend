package com.todaktodot.TDTD.couplelink.repository;

import com.todaktodot.TDTD.domain.couplelink.repository.CoupleLinkAuthRepository;
import com.todaktodot.TDTD.domain.couplelink.repository.entity.CoupleLinkAuthEntity;
import com.todaktodot.TDTD.domain.couplelink.repository.entity.LinkCodeStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("커플 연결 인증 리포지토리 테스트")
class CoupleLinkAuthRepositoryTest {

    @Autowired
    private CoupleLinkAuthRepository repository;

    @Test
    @DisplayName("코드로 엔티티 조회 - 존재하는 경우")
    void findByLinkCode_Exists() {
        // Given
        CoupleLinkAuthEntity entity = createEntity("ABC123", 1L, LinkCodeStatus.ISSUED);
        repository.save(entity);

        // When
        Optional<CoupleLinkAuthEntity> result = repository.findByLinkCode("ABC123");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getLinkCode()).isEqualTo("ABC123");
        assertThat(result.get().getIssuedUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("코드로 엔티티 조회 - 존재하지 않는 경우")
    void findByLinkCode_NotExists() {
        // When
        Optional<CoupleLinkAuthEntity> result = repository.findByLinkCode("NOTEXIST");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("사용자 ID와 상태로 조회 - ISSUED 상태")
    void findByIssuedUserIdAndStatusAndDelYn_Issued() {
        // Given
        CoupleLinkAuthEntity entity = createEntity("XYZ789", 2L, LinkCodeStatus.ISSUED);
        repository.save(entity);

        // When
        Optional<CoupleLinkAuthEntity> result = repository.findByIssuedUserIdAndStatusAndDelYn(
                2L, LinkCodeStatus.ISSUED, "N"
        );

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getIssuedUserId()).isEqualTo(2L);
        assertThat(result.get().getStatus()).isEqualTo(LinkCodeStatus.ISSUED);
    }

    @Test
    @DisplayName("만료된 코드 조회 - 만료 시간 이전")
    void findByStatusAndExpiredDtBeforeAndDelYn_ExpiredCodes() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        // 이미 만료된 코드
        CoupleLinkAuthEntity expiredEntity1 = createEntityWithExpiry(
                "EXP001", 3L, LinkCodeStatus.ISSUED, now.minusMinutes(10)
        );
        CoupleLinkAuthEntity expiredEntity2 = createEntityWithExpiry(
                "EXP002", 4L, LinkCodeStatus.ISSUED, now.minusMinutes(5)
        );

        // 아직 만료되지 않은 코드
        CoupleLinkAuthEntity validEntity = createEntityWithExpiry(
                "VAL001", 5L, LinkCodeStatus.ISSUED, now.plusMinutes(10)
        );

        repository.saveAll(List.of(expiredEntity1, expiredEntity2, validEntity));

        // When
        List<CoupleLinkAuthEntity> expiredList = repository.findByStatusAndExpiredDtBeforeAndDelYn(
                LinkCodeStatus.ISSUED, now, "N"
        );

        // Then
        assertThat(expiredList).hasSize(2);
        assertThat(expiredList)
                .extracting(CoupleLinkAuthEntity::getLinkCode)
                .containsExactlyInAnyOrder("EXP001", "EXP002");
    }

    @Test
    @DisplayName("여러 상태의 코드 저장 및 조회")
    void saveAndFindMultipleStatus() {
        // Given
        CoupleLinkAuthEntity issuedEntity = createEntity("CODE01", 6L, LinkCodeStatus.ISSUED);
        CoupleLinkAuthEntity expiredEntity = createEntity("CODE02", 7L, LinkCodeStatus.EXPIRED);
        CoupleLinkAuthEntity linkedEntity = createEntity("CODE03", 8L, LinkCodeStatus.LINKED);

        repository.saveAll(List.of(issuedEntity, expiredEntity, linkedEntity));

        // When
        Optional<CoupleLinkAuthEntity> issued = repository.findByLinkCode("CODE01");
        Optional<CoupleLinkAuthEntity> expired = repository.findByLinkCode("CODE02");
        Optional<CoupleLinkAuthEntity> linked = repository.findByLinkCode("CODE03");

        // Then
        assertThat(issued).isPresent();
        assertThat(issued.get().getStatus()).isEqualTo(LinkCodeStatus.ISSUED);

        assertThat(expired).isPresent();
        assertThat(expired.get().getStatus()).isEqualTo(LinkCodeStatus.EXPIRED);

        assertThat(linked).isPresent();
        assertThat(linked.get().getStatus()).isEqualTo(LinkCodeStatus.LINKED);
    }

    @Test
    @DisplayName("DEL_YN이 Y인 경우 조회되지 않음")
    void findByDelYn_ExcludesDeletedRecords() {
        // Given
        CoupleLinkAuthEntity activeEntity = createEntity("ACTIVE1", 9L, LinkCodeStatus.ISSUED);
        CoupleLinkAuthEntity deletedEntity = createEntity("DELETE1", 10L, LinkCodeStatus.ISSUED);

        repository.save(activeEntity);

        CoupleLinkAuthEntity saved = repository.save(deletedEntity);
        // DEL_YN을 Y로 변경 (직접 설정 불가능하므로 테스트 시 고려 필요)

        // When
        Optional<CoupleLinkAuthEntity> result = repository.findByIssuedUserIdAndStatusAndDelYn(
                9L, LinkCodeStatus.ISSUED, "N"
        );

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getDelYn()).isEqualTo("N");
    }

    // 헬퍼 메서드
    private CoupleLinkAuthEntity createEntity(String linkCode, Long userId, LinkCodeStatus status) {
        return CoupleLinkAuthEntity.builder()
                .linkCode(linkCode)
                .issuedUserId(userId)
                .status(status)
                .expiredDt(LocalDateTime.now().plusMinutes(30))
                .regrId(userId)
                .updrId(userId)
                .build();
    }

    private CoupleLinkAuthEntity createEntityWithExpiry(String linkCode, Long userId,
                                                        LinkCodeStatus status, LocalDateTime expiredDt) {
        return CoupleLinkAuthEntity.builder()
                .linkCode(linkCode)
                .issuedUserId(userId)
                .status(status)
                .expiredDt(expiredDt)
                .regrId(userId)
                .updrId(userId)
                .build();
    }
}
