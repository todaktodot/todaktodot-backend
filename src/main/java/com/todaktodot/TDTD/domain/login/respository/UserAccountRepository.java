package com.todaktodot.TDTD.domain.login.respository;

import com.todaktodot.TDTD.domain.login.respository.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByProviderIdAndProviderAndDelYn(String providerId, String provider, String delYn);
    Optional<UserAccount> findByUserIdAndDelYn(Long userId, String delYn);

    @Query("SELECT ua FROM UserAccount ua JOIN FETCH ua.user WHERE ua.providerId = :providerId AND ua.provider = :provider AND ua.delYn = :delYn")
    Optional<UserAccount> findByProviderIdAndProviderAndDelYnWithUser(@Param("providerId") String providerId, @Param("provider") String provider, @Param("delYn") String delYn);
}
