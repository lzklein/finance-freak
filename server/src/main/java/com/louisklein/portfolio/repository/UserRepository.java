package com.louisklein.portfolio.repository;

import com.louisklein.portfolio.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.email = :input OR u.username = :input")
    Optional<User> findByEmailOrUsername(@Param("input") String input);

    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE u.verified = false AND u.verificationTokenExpiresAt < :now")
    int deleteExpiredUnverifiedUsers(@Param("now") OffsetDateTime now);

    Optional<User> findByVerificationToken(String token);
}