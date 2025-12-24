package com.todaktodot.TDTD.domain.couplelink.repository;

import com.todaktodot.TDTD.domain.couplelink.repository.entity.CoupleLinkAuthEntity;
import com.todaktodot.TDTD.domain.couplelink.repository.entity.LinkStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CoupleLinkAuthRepository extends JpaRepository<CoupleLinkAuthEntity, Long> {

    // 코드로 조회
    Optional<CoupleLinkAuthEntity> findByLinkCode(String linkCode);

    // 사용자가 발급한 활성 코드 조회
    Optional<CoupleLinkAuthEntity> findByIssuedUserIdAndStatusAndDelYn(
            Long issuedUserId, LinkStatus status, String delYn
    );

    // 만료된 코드 조회 (배치 처리용)
    List<CoupleLinkAuthEntity> findByStatusAndExpiredDtBeforeAndDelYn(
            LinkStatus status, LocalDateTime now, String delYn
    );
}