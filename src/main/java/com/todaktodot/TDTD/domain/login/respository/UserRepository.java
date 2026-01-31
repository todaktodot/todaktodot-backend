package com.todaktodot.TDTD.domain.login.respository;

import com.todaktodot.TDTD.domain.login.respository.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByIdIn(Collection<Long> ids);
}
