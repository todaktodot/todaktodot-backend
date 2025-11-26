package com.todaktodot.TDTD.domain.login.respository;

import com.todaktodot.TDTD.domain.login.respository.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByProviderIdAndProvider(String id, String provider);
}
