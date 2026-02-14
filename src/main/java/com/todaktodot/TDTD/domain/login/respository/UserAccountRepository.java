package com.todaktodot.TDTD.domain.login.respository;

import com.todaktodot.TDTD.domain.login.respository.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByProviderIdAndProviderAndDelYn(String providerId, String provider, String delYn);
    Optional<UserAccount> findByUserIdAndDelYn(Long userId, String delYn);
}
