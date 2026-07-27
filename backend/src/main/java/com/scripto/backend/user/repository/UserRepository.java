package com.scripto.backend.user.repository;

import com.scripto.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);

    Optional<User> findOptionalByEmail(String email);

    List<User> findAllByActiveFalseAndDeletedAtBefore(LocalDateTime date);
}