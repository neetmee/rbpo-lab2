package com.rbpo.rbpo_lab2.repository;

import com.rbpo.rbpo_lab2.model.SessionStatus;
import com.rbpo.rbpo_lab2.model.UserSession;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM UserSession s WHERE s.refreshToken = :refreshToken")
    Optional<UserSession> findByRefreshToken(@Param("refreshToken") String refreshToken);

    @Modifying
    @Query("UPDATE UserSession s SET s.status = :revoked WHERE s.username = :username AND s.status = :active")
    int revokeAllActiveByUsername(
            @Param("username") String username,
            @Param("active") SessionStatus active,
            @Param("revoked") SessionStatus revoked
    );
}