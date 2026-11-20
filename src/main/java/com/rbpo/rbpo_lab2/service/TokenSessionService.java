package com.rbpo.rbpo_lab2.service;

import com.rbpo.rbpo_lab2.model.AccountStatus;
import com.rbpo.rbpo_lab2.model.SessionStatus;
import com.rbpo.rbpo_lab2.model.User;
import com.rbpo.rbpo_lab2.model.UserSession;
import com.rbpo.rbpo_lab2.repository.UserRepository;
import com.rbpo.rbpo_lab2.repository.UserSessionRepository;
import com.rbpo.rbpo_lab2.security.JwtTokenProvider;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class TokenSessionService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserSessionRepository sessionRepository;
    private final UserRepository userRepository;

    public TokenSessionService(
            JwtTokenProvider jwtTokenProvider,
            UserSessionRepository sessionRepository,
            UserRepository userRepository
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TokenPair issuePair(User user, String deviceId) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        return issuePairForUser(user, deviceId);
    }

    @Transactional(noRollbackFor = InvalidRefreshException.class)
    public TokenPair refresh(String refreshToken) {
        JwtTokenProvider.ParsedToken parsed;
        try {
            parsed = jwtTokenProvider.parseRefresh(refreshToken);
        } catch (JwtTokenProvider.InvalidJwtException ex) {
            throw new InvalidRefreshException("Invalid or expired refresh token", ex);
        }

        UserSession session = sessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new InvalidRefreshException("Refresh session not found"));

        if (session.getStatus() == SessionStatus.REVOKED) {
            throw new InvalidRefreshException("Refresh session is revoked");
        }

        if (session.getStatus() == SessionStatus.USED) {
            session.setStatus(SessionStatus.REVOKED);
            sessionRepository.saveAndFlush(session);
            throw new InvalidRefreshException("Refresh token has already been used");
        }

        Instant now = Instant.now();
        if (session.getStatus() != SessionStatus.ACTIVE
                || !parsed.subject().equals(session.getUsername())
                || !session.getRefreshTokenExpiry().isAfter(now)) {
            session.setStatus(SessionStatus.REVOKED);
            sessionRepository.saveAndFlush(session);
            throw new InvalidRefreshException("Refresh session is no longer valid");
        }

        User user = userRepository.findByUsername(session.getUsername())
                .orElseThrow(() -> new InvalidRefreshException("User not found"));

        if (user.getStatus() != AccountStatus.ACTIVE) {
            session.setStatus(SessionStatus.REVOKED);
            sessionRepository.saveAndFlush(session);
            throw new InvalidRefreshException("User account is not active");
        }

        session.setStatus(SessionStatus.USED);
        sessionRepository.saveAndFlush(session);

        return issuePairForUser(user, session.getDeviceId());
    }

    private TokenPair issuePairForUser(User user, String deviceId) {
        String normalizedDeviceId = (deviceId == null || deviceId.isBlank()) ? null : deviceId.trim();

        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        JwtTokenProvider.IssuedToken access = jwtTokenProvider.createAccessToken(user.getUsername(), authorities);
        JwtTokenProvider.IssuedToken refresh = jwtTokenProvider.createRefreshToken(user.getUsername());

        UserSession newSession = new UserSession(
                user.getUsername(),
                normalizedDeviceId,
                access.token(),
                refresh.token(),
                access.expiresAt(),
                refresh.expiresAt(),
                SessionStatus.ACTIVE
        );

        sessionRepository.saveAndFlush(newSession);
        return new TokenPair(access.token(), refresh.token());
    }

    public record TokenPair(String accessToken, String refreshToken) {
    }

    public static class InvalidRefreshException extends RuntimeException {
        public InvalidRefreshException(String message) {
            super(message);
        }

        public InvalidRefreshException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}