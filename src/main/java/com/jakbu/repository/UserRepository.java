package com.jakbu.repository;

import com.jakbu.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByAccountId(String accountId);
    Optional<User> findByRefreshToken(String refreshToken);
}

